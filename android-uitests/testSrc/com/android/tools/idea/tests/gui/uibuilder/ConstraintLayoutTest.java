/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.tests.gui.uibuilder;

import com.android.tools.idea.editors.theme.ui.ResourceComponent;
import com.android.tools.idea.tests.gui.framework.*;
import com.android.tools.idea.tests.gui.framework.fixture.ChooseResourceDialogFixture;
import com.android.tools.idea.tests.gui.framework.fixture.EditorFixture;
import com.android.tools.idea.tests.gui.framework.fixture.IdeFrameFixture;
import com.android.tools.idea.tests.gui.framework.fixture.designer.NlEditorFixture;
import com.android.tools.idea.tests.gui.framework.fixture.designer.layout.NlPreviewFixture;
import com.android.tools.idea.tests.gui.framework.fixture.theme.*;
import com.android.tools.idea.tests.gui.theme.ThemeEditorGuiTestUtils;
import com.android.tools.adtui.TextAccessors;
import com.intellij.BundleBase;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.data.TableCell;
import org.fest.swing.fixture.JTableCellFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.fest.swing.data.TableCell.row;

/**
 * UI tests for the constraint layout
 */
@RunWith(GuiTestRunner.class)
public class ConstraintLayoutTest {

  @Rule public final GuiTestRule guiTest = new GuiTestRule();

  /**
   * Verifies the UI for adding side constraints for a ConstraintLayout in the layout editor.
   * <p>
   * This is run to qualify releases. Please involve the test team in substantial changes.
   * <p>
   * TT ID: 7f7d5b54-a3df-41cc-a472-fa6f50296b0b
   * <p>
   *   <pre>
   *   Test Steps:
   *   1. Open the layout file which uses constraint layout and switch to design view
   *   2. From the palette window, drag and drop couple of widgets to constraint layout. Say, a button and a text view
   *   3. Now click on a widget in the design view, select any of the little circles on it and create constraints for all left, right,
   *      top and bottom. Create them such that your refer both other widgets and the parent layout (Verify 1)
   *   4. Repeat steps 3 and 4 by aligning widgets in blue print mode (Verify 1)
   *   Verify:
   *   1. Should be able to position the widgets in both Deign view and blueprint mode. Verify the constraints (app:layout_constraint***)
   *      in xml view of layout editor.
   *   </pre>
   */
  @RunIn(TestGroup.QA)
  @Test
  public void testSideConstraintHandling() throws Exception {
    EditorFixture editor = guiTest.importProjectAndWaitForProjectSyncToFinish("LayoutTest")
      .getEditor()
      .open("app/src/main/res/layout/constraint.xml", EditorFixture.Tab.DESIGN);
    NlEditorFixture layoutEditor = editor.getLayoutEditor(true);

    layoutEditor
      .showOnlyDesignView()
      .dragComponentToSurface("Widgets", "Button")
      .findView("Button", 0)
      .createConstraintFromBottomToTopOf(layoutEditor.findView("TextView", 0))
      .createConstraintFromTopToTopOfLayout()
      .createConstraintFromLeftToLeftOfLayout()
      .createConstraintFromRightToRightOfLayout();

    String layoutContents = editor.selectEditorTab(EditorFixture.Tab.EDITOR).getCurrentFileContents();
    assertThat(layoutContents).contains("app:layout_constraintBottom_toTopOf=\"@+id/textView\"");
    assertThat(layoutContents).contains("app:layout_constraintTop_toTopOf=\"parent\"");
    assertThat(layoutContents).contains("app:layout_constraintLeft_toLeftOf=\"parent\"");
    assertThat(layoutContents).contains("app:layout_constraintRight_toRightOf=\"parent\"");

    layoutEditor = editor.select("(<Button[\\s\\S]*/>\\n)")
      .invokeAction(EditorFixture.EditorAction.BACK_SPACE)
      .getLayoutEditor(true);

    layoutEditor
      .showOnlyBlueprintView()
      .dragComponentToSurface("Widgets", "Button")
      .findView("Button", 0)
      .createConstraintFromBottomToTopOf(layoutEditor.findView("TextView", 0))
      .createConstraintFromTopToTopOfLayout()
      .createConstraintFromLeftToLeftOfLayout()
      .createConstraintFromRightToRightOfLayout();

    layoutContents = editor.selectEditorTab(EditorFixture.Tab.EDITOR).getCurrentFileContents();
    assertThat(layoutContents).contains("app:layout_constraintBottom_toTopOf=\"@+id/textView\"");
    assertThat(layoutContents).contains("app:layout_constraintTop_toTopOf=\"parent\"");
    assertThat(layoutContents).contains("app:layout_constraintLeft_toLeftOf=\"parent\"");
    assertThat(layoutContents).contains("app:layout_constraintRight_toRightOf=\"parent\"");
  }

  /**
   * Verifies the UI for adding baseline constraints for a ConstraintLayout in the layout editor.
   * <p>
   * This is run to qualify releases. Please involve the test team in substantial changes.
   * <p>
   * TT ID: 604b3f71-f831-4236-a30e-725fa0c54193
   * <p>
   *   <pre>
   *   Test Steps:
   *   1. Open the layout file which uses constraint layout and switch to design view
   *   2. From the palette window, drag and drop couple of widgets to constraint layout. Say, a button and a text view
   *   3. Now click on a widget in the design view, you should see a wide and thin rectangle.
   *   4. Click on the thin rectangle of one widget and create a constraint with similar rectangle in another widget
   *   5. Repeat steps 3 and 4 by aligning widgets in blue print mode (Verify 1)
   *   Verify:
   *   1. Should be able to baseline the widgets in both design view and blueprint mode. Verfiy the same in xml view by checking
   *      for "layout_constraintBaseline_toBaselineOf"
   *   </pre>
   */
  @RunIn(TestGroup.QA)
  @Test
  public void testBaselineConstraintHandling() throws Exception {
    EditorFixture editor = guiTest.importProjectAndWaitForProjectSyncToFinish("LayoutTest")
      .getEditor()
      .open("app/src/main/res/layout/constraint.xml", EditorFixture.Tab.DESIGN);
    NlEditorFixture layoutEditor = editor.getLayoutEditor(true);

    layoutEditor
      .waitForRenderToFinish()
      .showOnlyDesignView()
      .dragComponentToSurface("Widgets", "Button")
      .findView("Button", 0)
      .createBaselineConstraintWith(layoutEditor.findView("TextView", 0));
    String layoutContents = editor.selectEditorTab(EditorFixture.Tab.EDITOR).getCurrentFileContents();
    assertThat(layoutContents).contains("app:layout_constraintBaseline_toBaselineOf=\"@+id/textView\"");

    layoutEditor = editor.select("(<Button[\\s\\S]*/>\\n)")
      .invokeAction(EditorFixture.EditorAction.BACK_SPACE)
      .getLayoutEditor(true);

    layoutEditor
      .showOnlyBlueprintView()
      .dragComponentToSurface("Widgets", "Button")
      .findView("Button", 0)
      .createBaselineConstraintWith(layoutEditor.findView("TextView", 0));
    layoutContents = editor.selectEditorTab(EditorFixture.Tab.EDITOR).getCurrentFileContents();
    assertThat(layoutContents).contains("app:layout_constraintBaseline_toBaselineOf=\"@+id/textView\"");
  }

  /**
   * To verify that items from the tool kit can be added to a layout.
   * <p>
   * This is run to qualify releases. Please involve the test team in substantial changes.
   * <p>
   * TT ID: 7fd4834b-ef5a-4414-b601-3c8bd8ab54d0
   * <p>
   *   <pre>
   *   Test Steps:
   *   1. Open layout editor for the default activity.
   *   2. Add each item from the toolbar.
   *   3. Switch to xml view.
   *   Verify:
   *   1. Verify the item displays in the xml view.
   *   </pre>
   */
  @RunIn(TestGroup.QA)
  @Test
  public void addAllLayoutItemsFromToolbar() throws Exception {
    IdeFrameFixture ideFrameFixture = guiTest.importSimpleApplication();

    String group = "Widgets";

    NlEditorFixture design = ideFrameFixture.getEditor()
      .open("app/src/main/res/layout/activity_my.xml", EditorFixture.Tab.DESIGN)
      .getLayoutEditor(false);

    List<String> widgets =
      Arrays.asList("Button", "ToggleButton", "CheckBox", "RadioButton", "CheckedTextView",
                    "Spinner", "ProgressBar", "SeekBar", "RatingBar", "Switch", "Space");
    for (String widget : widgets) {
      design.dragComponentToSurface(group, widget);
    }
    String layoutXml = ideFrameFixture
      .getEditor()
      .open("app/src/main/res/layout/activity_my.xml", EditorFixture.Tab.EDITOR)
      .getCurrentFileContents();
    for (String widget: widgets) {
      assertThat(layoutXml).containsMatch("<" + widget);
    }
  }

  /**
   * To verify that the layout preview renders appropriately with different themes and API selections
   * <p>
   * This is run to qualify releases. Please involve the test team in substantial changes.
   * <p>
   * TT ID: d45e0fa5-82d5-4d9a-9046-0437210741f0
   * <p>
   *   <pre>
   *   Test Steps:
   *   1. Open layout.xml design view for the MainActivity
   *   2. Select the rendering device and switch to another device, say N5 and repeat with N6 (Verify 1)
   *   3. Click on the Orientation and repeat (Verify 2)
   *   4. Select the Android version option and choose older API levels one by one (Verify 3)
   *   5. Select the Theme option and choose a different theme to render the preview (Verify 4)
   *   6. Select the activity option and choose a different activity (Verify 5)
   *   Verify:
   *   1. Preview render device changes to the newly selected device.
   *   2. The preview layout orientation switches to landscape and then back to Portrait.
   *   3. Preview layout renders fine on compatible API levels.
   *   4. The selected theme is applied on the preview layout.
   *   5. Preview layout is rendered for the selected activity.
   *   </pre>
   */
  @RunIn(TestGroup.QA)
  @Test
  public void layoutPreviewRendering() throws Exception {
    IdeFrameFixture ideFrameFixture = guiTest.importProjectAndWaitForProjectSyncToFinish("LayoutTest");

    EditorFixture editorFixture = ideFrameFixture.getEditor()
      .open("app/src/main/res/layout/layout2.xml", EditorFixture.Tab.DESIGN);

    NlPreviewFixture preview = editorFixture
      .getLayoutPreview(true)
      .waitForRenderToFinish();

    preview.getConfigToolbar()
      .chooseDevice("Nexus 5")
      .requireDevice("Nexus 5")
      .chooseDevice("Nexus 6")
      .requireDevice("Nexus 6");

    preview.getConfigToolbar()
      .switchOrientation()
      .leaveConfigToolbar()
      .waitForRenderToFinish()
      .getConfigToolbar()
      .requireOrientation("Landscape");

    preview.getConfigToolbar()
      .switchOrientation()
      .leaveConfigToolbar()
      .waitForRenderToFinish()
      .getConfigToolbar()
      .requireOrientation("Portrait");

    preview.getConfigToolbar()
      .chooseApiLevel("API 23")
      .requireApiLevel("23")
      .chooseApiLevel("API 24")
      .requireApiLevel("24")
      .chooseApiLevel("API 25")
      .requireApiLevel("25");

    preview.getConfigToolbar()
      .openThemeSelectionDialog()
      .selectsTheme("Material Light", "android:Theme.Material.Light")
      .clickOk();
    preview.getConfigToolbar()
      .requireTheme("Light");
    preview.getConfigToolbar()
      .openThemeSelectionDialog()
      .selectsTheme("Material Dark", "android:Theme.Material")
      .clickOk();
    preview.getConfigToolbar()
      .requireTheme("Material");

    editorFixture = ideFrameFixture.getEditor()
      .open("app/src/main/res/layout/layout1.xml", EditorFixture.Tab.DESIGN);

    preview = editorFixture
      .getLayoutPreview(true)
      .waitForRenderToFinish();

    preview.getConfigToolbar()
      .requireDevice("Nexus 6")
      .requireOrientation("Portrait")
      .requireApiLevel("25")
      .requireTheme("AppTheme");
  }

  /**
   * To verify that the layout preview renders appropriately with different themes and API selections
   * <p>
   * This is run to qualify releases. Please involve the test team in substantial changes.
   * <p>
   * TT ID: 7ba5466f-78b0-43fa-8739-602f35c444d8
   * <p>
   *   <pre>
   *   Test Steps:
   *   1. Open Android Studio and import a simple application
   *   2. Open Theme Editor (Verify 1) (Tools > Android > Theme Editor)
   *   3. From the right pane, select a different theme from the list , say a dark theme like Material Dark .(Verify 2)
   *   4. Choose a different API from the list, say Android 5.0 or Android 4.0.3 (Verify 3)
   *   5. Choose a different device from the list, say N5 or N6 (Verify 4)
   *   6. Switch between portrait and landscape modes (Verify 5)
   *   7. Select Theme and then click on New Theme and enter a name and choose a parent theme from the list and click OK (Verify 6)
   *   8. On the newly created theme, click on a resource , say android:colorBackground and select a different color from the color picker (Verify 7)
   *   9. Repeat step 8 with a default system theme, like Material Light (Verify 8)
   *   Verify:
   *   1. Preview is displayed with the default theme selection (App Theme)
   *   2. Preview is updated with the newly selected theme
   *   3. Preview is updated for the other API's and there are no rendering errors
   *   4. Preview is updated for the other devices and there are no rendering errors
   *   5. Preview is updated appropriately without any errors
   *   6. New theme is created and is displayed as the selected theme for the module.
   *   7. Preview is updated with the newly selected color for the background
   *   8. A prompt will be displayed to the user mentioning that that the selected system theme is ready-only
   *   and that they need to create a new theme with the selected background color.
   *   </pre>
   */
  @RunIn(TestGroup.QA)
  @Test
  public void themeEditor() throws Exception {
    guiTest.importSimpleApplication();
    ThemeEditorFixture themeEditor = ThemeEditorGuiTestUtils.openThemeEditor(guiTest.ideFrame());
    ThemeEditorTableFixture themeEditorTable = themeEditor.getPropertiesTable();

    themeEditor.chooseTheme("Theme.AppCompat.NoActionBar")
      .chooseApiLevel("API 25", "25")
      .chooseDevice("Nexus 5", "Nexus 5")
      .switchOrientation("Landscape")
      .switchOrientation("Portrait")
      .createNewTheme("NewTheme", "Theme.AppCompat.NoActionBar");

    TableCell cell = row(1).column(0);
    JTableCellFixture colorCell = themeEditorTable.cell(cell);

    ResourceComponentFixture resourceComponent = new ResourceComponentFixture(guiTest.robot(), (ResourceComponent)colorCell.editor());
    colorCell.startEditing();
    resourceComponent.getSwatchButton().click();
    ChooseResourceDialogFixture dialog = ChooseResourceDialogFixture.find(guiTest.robot());
    Color color = new Color(255, 235, 59, 255);
    dialog.getColorPicker().setColorWithIntegers(color);
    dialog.clickOK();
    colorCell.stopEditing();

    themeEditor.chooseTheme("Theme.AppCompat.NoActionBar");
    colorCell.startEditing();
    resourceComponent.getSwatchButton().click();
    dialog = ChooseResourceDialogFixture.find(guiTest.robot());
    dialog.clickOK();
    NewThemeDialogFixture newThemeDialog = NewThemeDialogFixture.findDialog(guiTest.robot());
    GuiTests.waitUntilShowing(guiTest.robot(), newThemeDialog.target(), new GenericTypeMatcher<JLabel>(JLabel.class) {
      @Override
      protected boolean isMatching(@NotNull JLabel component) {
        String componentText = TextAccessors.getTextAccessor(component).getText();
        componentText = componentText == null ? "" : componentText.replaceAll(Character.toString(BundleBase.MNEMONIC), "");
        return componentText.contains("Read-Only");
      }
    });
    newThemeDialog.clickCancel();
    colorCell.stopEditing();
  }
}
