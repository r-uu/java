package de.ruu.app.pragma.fx.task.edit;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.lib.fx.comp.FXCService;
import javafx.beans.property.BooleanProperty;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.Consumer;

public interface TaskEditorService extends FXCService
{
  Optional<TaskBean> task();
  void task(TaskBean task);
  void clear();
  void applyTo(TaskBean task);
  void setEditable(boolean editable);
  void onTaskUpdated(Consumer<TaskBean> listener);
  BooleanProperty dirtyProperty();
  void clearDirty();
  boolean isUpdating();
  void beginUpdating();
  void endUpdating();
}
