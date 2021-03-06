package org.jetbrains.android.database;

import com.intellij.database.DatabaseMessages;
import com.intellij.database.dataSource.DataSourceTemplate;
import com.intellij.database.dialects.DatabaseDialectEx;
import com.intellij.database.dialects.SqliteDialect;
import com.intellij.database.model.DatabaseSystem;
import com.intellij.database.psi.BasicDbPsiManager;
import com.intellij.database.util.DbSqlUtil;
import com.intellij.facet.ProjectFacetManager;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.undo.GlobalUndoableAction;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.command.undo.UnexpectedUndoException;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.sql.dialects.SqlLanguageDialect;
import icons.AndroidIcons;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Eugene.Kudelevsky
 */
public class AndroidDbManager extends BasicDbPsiManager<AndroidDataSource> {
  public static final String NOTIFICATION_GROUP_ID = "Android Data Source Manager";
  static final DataSourceTemplate DEFAULT_TEMPLATE = new AndroidDataSourceTemplate();

  public AndroidDbManager(@NotNull Project project) {
    super(project, AndroidDataSourceStorage.getInstance(project).getDataSources());
  }

  @Nullable
  @Override
  public DatabaseDialectEx getDatabaseDialect(@NotNull DatabaseSystem element) {
    return SqliteDialect.INSTANCE;
  }

  @Nullable
  @Override
  public SqlLanguageDialect getSqlDialect(@NotNull DatabaseSystem element) {
    return DbSqlUtil.findSqlDialect(SqliteDialect.INSTANCE);
  }

  @Override
  public void setDataSourceName(@NotNull DatabaseSystem element, String name) {
    if (!(element instanceof AndroidDataSource)) throw new UnsupportedOperationException();
    final AndroidDataSource dataSource = (AndroidDataSource)element;
    dataSource.setName(name);
    myPublisher.dataSourceChanged(this, element);
  }

  @Override
  public void removeDataSource(DatabaseSystem element) {
    if (!(element instanceof AndroidDataSource)) throw new UnsupportedOperationException();
    final AndroidDataSource dataSource = (AndroidDataSource)element;
    processAddOrRemove(dataSource, false);
  }

  @NotNull
  @Override
  public Configurable createDataSourceEditor(DatabaseSystem template) {
    if (!(template instanceof AndroidDataSource)) throw new UnsupportedOperationException();
    AndroidDataSource dataSource = (AndroidDataSource)template;
    return new AndroidDataSourceConfigurable(this, myProject, dataSource);
  }

  @NotNull
  @Override
  public List<DataSourceTemplate> getDataSourceTemplates() {
    if (ProjectFacetManager.getInstance(myProject).hasFacets(AndroidFacet.ID)) {
      return Collections.singletonList(DEFAULT_TEMPLATE);
    }
    else {
      return Collections.emptyList();
    }
  }

  @Nullable
  @Override
  public DataSourceTemplate getDataSourceTemplate(DatabaseSystem element) {
    return DEFAULT_TEMPLATE;
  }

  public void processAddOrRemove(final AndroidDataSource dataSource, final boolean add) {
    final UndoableAction action = new GlobalUndoableAction() {
      public void undo() throws UnexpectedUndoException {
        doIt(!add);
      }

      public void redo() throws UnexpectedUndoException {
        doIt(add);
      }

      private void doIt(boolean add) {
        if (add) {
          addDataSourceInner(myProject, dataSource);
        }
        else {
          removeDataSourceInner(myProject, dataSource);
        }
      }
    };
    try {
      WriteCommandAction.writeCommandAction(myProject)
                        .withName(add ? DatabaseMessages.message("command.name.add.data.source")
                                      : DatabaseMessages.message("command.name.remove.data.source"))
                        .withUndoConfirmationPolicy(UndoConfirmationPolicy.REQUEST_CONFIRMATION)
                        .run(() -> {
                          action.redo();
                          UndoManager.getInstance(myProject).undoableActionPerformed(action);
                        });
    }
    catch (UnexpectedUndoException e) {
      throw new RuntimeException(e);
    }
  }

  private void removeDataSourceInner(final Project project, final AndroidDataSource dataSource) {
    AndroidDataSourceStorage storage = AndroidDataSourceStorage.getInstance(project);
    storage.removeDataSource(dataSource);
    detachDataSource(dataSource);
    myPublisher.dataSourceRemoved(this, dataSource);
  }

  private void addDataSourceInner(final Project project, final AndroidDataSource dataSource) {
    AndroidDataSourceStorage storage = AndroidDataSourceStorage.getInstance(project);
    storage.addDataSource(dataSource);
    attachDataSource(dataSource);
    myPublisher.dataSourceAdded(this, dataSource);
  }

  @Override
  public boolean canCreateDataSourceByFiles(Collection<VirtualFile> files) {
    return false;
  }

  @NotNull
  @Override
  public Collection<AndroidDataSource> createDataSourceByFiles(Collection<VirtualFile> files) {
    return Collections.emptyList();
  }

  @Override
  public void addDataSource(@NotNull AndroidDataSource dataSource) {
    processAddOrRemove(dataSource, true);
  }

  private static class AndroidDataSourceTemplate implements DataSourceTemplate {
    @NotNull
    @Override
    public String getName() {
      return "Android SQLite";
    }

    @NotNull
    @Override
    public String getFullName() {
      return getName();
    }

    @NotNull
    @Override
    public List<DataSourceTemplate> getSubConfigurations() {
      return Collections.emptyList();
    }

    @NotNull
    @Override
    public DatabaseSystem createDataSource(@NotNull Project project, @Nullable DatabaseSystem copyFrom, @Nullable String newName) {
      AndroidDataSource result;
      if (copyFrom instanceof AndroidDataSource) {
        result = ((AndroidDataSource)copyFrom).copy();
      }
      else {
        result = new AndroidDataSource();
      }
      result.setName(StringUtil.notNullize(newName, getName()));
      result.resolveDriver();
      return result;
    }

    @Override
    public Icon getIcon(@IconFlags int flags) {
      return AndroidIcons.Android;
    }
  }
}
