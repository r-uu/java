package de.ruu.app.pragma.fx.task.edit;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.lib.fx.comp.FXCService;
import javafx.beans.property.BooleanProperty;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public interface TaskEditorService extends FXCService
{
  Optional<TaskBean> task();
  void task(TaskBean task);
  void clear();
  void applyTo(TaskBean task);
  void setEditable(boolean editable);
  BooleanProperty dirtyProperty();
  void clearDirty();
  boolean isUpdating();
  void beginUpdating();
  void endUpdating();
}
