package de.ruu.app.pragma.fx.taskgroup.view;

import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.lib.fx.comp.FXCService;
import javafx.beans.property.BooleanProperty;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public interface TaskGroupViewService extends FXCService
{
  @NonNull Optional<TaskGroupBean> taskGroup();
  void taskGroup(TaskGroupBean taskGroup);
  void clear();
  void applyTo(TaskGroupBean taskGroup);
  void setEditable(boolean editable);
  BooleanProperty dirtyProperty();
  void clearDirty();
}
