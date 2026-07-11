package de.ruu.app.pragma.fx.task.graph;

import de.ruu.app.pragma.bean.TaskBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Stateless layout algorithms for the dependency graph — no JavaFX dependency.
 * Extracted from GraphController to allow unit-testing without a JavaFX runtime.
 */
public final class GraphLayout
{
    public static final double PAD  = 20.0;
    public static final double STEP = 100.0; // NODE_HEIGHT(60) + V_GAP(40)

    private GraphLayout() {}

    /**
     * Column assignment: max of predecessor depth (topological) and parent-child depth.
     * Root tasks (no parent, no predecessors) end up in column 0 (leftmost).
     * Successors and children are placed in higher columns to the right.
     */
    public static Map<Long, Integer> computeLayers(Map<Long, TaskBean> byId)
    {
        Map<Long, Integer> predLayers   = computePredecessorLayers(byId);
        Map<Long, Integer> parentDepths = computeParentDepths(byId);

        Map<Long, Integer> result = new HashMap<>();
        byId.keySet().forEach(id ->
            result.put(id, Math.max(
                predLayers.getOrDefault(id, 0),
                parentDepths.getOrDefault(id, 0))));
        return result;
    }

    /**
     * Topological layer assignment via Kahn's algorithm.
     * Ghost predecessors (ids not in {@code byId}) are ignored.
     */
    private static Map<Long, Integer> computePredecessorLayers(Map<Long, TaskBean> byId)
    {
        Map<Long, Integer>    inDegree   = new HashMap<>();
        Map<Long, List<Long>> successors = new HashMap<>();

        for (TaskBean t : byId.values())
        {
            inDegree.putIfAbsent(t.id(), 0);
            t.predecessors().ifPresent(preds ->
            {
                for (TaskBean pred : preds)
                {
                    if (pred.id() == null)            continue;
                    if (!byId.containsKey(pred.id())) continue; // ghost → skip
                    inDegree.merge(t.id(), 1, Integer::sum);
                    successors.computeIfAbsent(pred.id(), k -> new ArrayList<>()).add(t.id());
                }
            });
        }

        Map<Long, Integer> layer = new HashMap<>();
        Queue<Long>        queue = new LinkedList<>();
        for (Map.Entry<Long, Integer> e : inDegree.entrySet())
            if (e.getValue() == 0) { queue.add(e.getKey()); layer.put(e.getKey(), 0); }

        Set<Long> visited = new HashSet<>();
        while (!queue.isEmpty())
        {
            Long current = queue.poll();
            if (!visited.add(current)) continue;
            int currentLayer = layer.getOrDefault(current, 0);
            for (Long succId : successors.getOrDefault(current, List.of()))
            {
                int newLayer = currentLayer + 1;
                if (newLayer > layer.getOrDefault(succId, 0)) layer.put(succId, newLayer);
                queue.add(succId);
            }
        }

        byId.keySet().forEach(id -> layer.putIfAbsent(id, 0));
        return layer;
    }

    /**
     * Derives each task's depth in the parent-child hierarchy from the loaded {@code subTasks}
     * relation (parentTask is not loaded by the REST endpoint, but subTasks is).
     * Root tasks (not a subTask of any other task in the group) get depth 0.
     */
    private static Map<Long, Integer> computeParentDepths(Map<Long, TaskBean> byId)
    {
        Map<Long, Long> childToParent = new HashMap<>();
        for (TaskBean t : byId.values())
        {
            t.subTasks().ifPresent(children ->
                children.stream()
                        .filter(c -> c.id() != null && byId.containsKey(c.id()))
                        .forEach(c -> childToParent.put(c.id(), t.id())));
        }

        Map<Long, Integer> depth = new HashMap<>();
        byId.keySet().forEach(id -> depth.put(id, parentDepth(id, childToParent, new HashSet<>())));
        return depth;
    }

    private static int parentDepth(Long id, Map<Long, Long> childToParent, Set<Long> visiting)
    {
        if (!visiting.add(id)) return 0;
        Long parentId = childToParent.get(id);
        if (parentId == null) return 0;
        return 1 + parentDepth(parentId, childToParent, visiting);
    }

    /**
     * Returns the average y-position of already-placed predecessors within the same group.
     * Falls back to {@link #PAD} when no predecessor has been placed yet.
     */
    public static double avgPredY(Long id, Map<Long, TaskBean> byId, Map<Long, Double> yPos)
    {
        TaskBean task = byId.get(id);
        if (task == null) return PAD;
        List<Double> ys = new ArrayList<>();
        task.predecessors().ifPresent(preds ->
            preds.stream()
                 .filter(p -> p.id() != null && yPos.containsKey(p.id()))
                 .forEach(p -> ys.add(yPos.get(p.id())))
        );
        return ys.isEmpty() ? PAD : ys.stream().mapToDouble(d -> d).average().orElse(PAD);
    }

    /**
     * Spreads a sorted list of target y-positions so that consecutive entries are
     * at least {@link #STEP} apart. Forward pass pushes nodes down; backward pass
     * pulls them back up as close as possible to their targets.
     * Result is clamped to ≥ {@link #PAD}.
     */
    public static List<Double> resolveOverlaps(List<Double> targets)
    {
        List<Double> r = new ArrayList<>(targets);

        for (int i = 1; i < r.size(); i++)                     // push down
            if (r.get(i) < r.get(i - 1) + STEP) r.set(i, r.get(i - 1) + STEP);

        for (int i = r.size() - 2; i >= 0; i--)               // pull up
            r.set(i, Math.min(r.get(i), r.get(i + 1) - STEP));

        double minY = r.stream().mapToDouble(d -> d).min().orElse(PAD);
        if (minY < PAD) { double shift = PAD - minY; r.replaceAll(v -> v + shift); }

        return r;
    }
}
