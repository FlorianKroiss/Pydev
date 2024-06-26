/**
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.ui.importsconf;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.python.pydev.core.imports.ImportPreferences;
import org.python.pydev.plugin.PyDevUiPrefs;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.shared_core.SharedCorePlugin;
import org.python.pydev.shared_core.string.WrapAndCaseUtils;
import org.python.pydev.shared_ui.field_editors.BooleanFieldEditorCustom;
import org.python.pydev.shared_ui.field_editors.ComboFieldEditor;
import org.python.pydev.shared_ui.field_editors.CustomStringFieldEditor;
import org.python.pydev.shared_ui.field_editors.FileFieldEditorCustom;
import org.python.pydev.shared_ui.field_editors.LabelFieldEditor;
import org.python.pydev.shared_ui.field_editors.LinkFieldEditor;
import org.python.pydev.shared_ui.field_editors.RadioGroupFieldEditor;
import org.python.pydev.shared_ui.field_editors.ScopedFieldEditorPreferencePage;
import org.python.pydev.shared_ui.field_editors.ScopedPreferencesFieldEditor;

/**
 * Preferences regarding the way that imports should be managed:
 *
 * - Grouped when possible?
 * - Can use multilines?
 * - Multilines with escape char or with '('
 *
 * @author Fabio
 */
public class ImportsPreferencesPage extends ScopedFieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditorCustom fromImportsFirstBooleanEditor;
    private ComboFieldEditor importEngineFieldEditor;
    private BooleanFieldEditorCustom deleteUnusedImportsField;
    private BooleanFieldEditorCustom groupImportsField;
    private BooleanFieldEditorCustom multilineImportsField;
    private BooleanFieldEditorCustom sortIndiviualOnGroupedField;
    private RadioGroupFieldEditor breakImportsInMultilineMode;
    private RadioGroupFieldEditor isortFormatterLocation;
    private FileFieldEditorCustom isortFileField;
    private CustomStringFieldEditor isortParameters;

    public static final String[][] SEARCH_FORMATTER_LOCATION_OPTIONS = new String[][] {
            { "Search in interpreter", ImportPreferences.LOCATION_SEARCH },
            { "Specify Location", ImportPreferences.LOCATION_SPECIFY },
    };

    public ImportsPreferencesPage() {
        super(FLAT);
        setPreferenceStore(PydevPlugin.getDefault().getPreferenceStore());
        setDescription("Imports Preferences");
    }

    @Override
    protected void createFieldEditors() {
        final Composite p = getFieldEditorParent();

        addField(new LabelFieldEditor("Label_Info_File_Preferences1", WrapAndCaseUtils.wrap(
                "These setting are used whenever imports are managed in the application\n\n", 80), p));

        importEngineFieldEditor = new ComboFieldEditor(ImportPreferences.IMPORT_ENGINE,
                "Select import sort engine to be used",
                new String[][] {
                        new String[] { "Pep 8", ImportPreferences.IMPORT_ENGINE_PEP_8 },
                        new String[] { "Regular sort", ImportPreferences.IMPORT_ENGINE_REGULAR_SORT },
                        new String[] { "isort", ImportPreferences.IMPORT_ENGINE_ISORT },
                }, p);
        addFieldWithToolTip(importEngineFieldEditor, p,
                "Select which import engine should be used to sort the imports when such an operation is requested.");

        isortFormatterLocation = new RadioGroupFieldEditor(ImportPreferences.ISORT_LOCATION_OPTION,
                "isort executable", 2, SEARCH_FORMATTER_LOCATION_OPTIONS, p);

        for (Button b : isortFormatterLocation.getRadioButtons()) {
            b.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateEnablement(p, importEngineFieldEditor.getComboValue());
                }
            });
        }

        addField(isortFormatterLocation);
        isortFileField = new FileFieldEditorCustom(ImportPreferences.ISORT_FILE_LOCATION,
                "Location of the isort executable:", p, 1);
        addField(isortFileField);

        isortParameters = new CustomStringFieldEditor(ImportPreferences.ISORT_PARAMETERS, "Parameters for isort", p);
        addField(isortParameters);

        deleteUnusedImportsField = new BooleanFieldEditorCustom(ImportPreferences.DELETE_UNUSED_IMPORTS,
                WrapAndCaseUtils.wrap(
                        "Delete unused imports?", 80),
                p);
        addFieldWithToolTip(
                deleteUnusedImportsField,
                p,
                "Simple unused imports as reported by the code analysis are deleted. This can be configured to ignore certain files, and individual warnings can be surpressed.");

        groupImportsField = new BooleanFieldEditorCustom(ImportPreferences.GROUP_IMPORTS,
                "Combine 'from' imports when possible?", p);
        addField(groupImportsField);

        fromImportsFirstBooleanEditor = new BooleanFieldEditorCustom(ImportPreferences.FROM_IMPORTS_FIRST,
                "Sort 'from' imports before 'import' imports?", p);
        addField(fromImportsFirstBooleanEditor);

        multilineImportsField = new BooleanFieldEditorCustom(ImportPreferences.MULTILINE_IMPORTS,
                WrapAndCaseUtils.wrap(
                        "Allow multiline imports when the import size would exceed the print margin?", 80),
                p);
        addField(multilineImportsField);

        sortIndiviualOnGroupedField = new BooleanFieldEditorCustom(ImportPreferences.SORT_NAMES_GROUPED,
                WrapAndCaseUtils.wrap(
                        "Sort individual names on grouped imports?", 80),
                p);
        addField(sortIndiviualOnGroupedField);

        breakImportsInMultilineMode = new RadioGroupFieldEditor(ImportPreferences.BREAK_IMPORTS_MODE,
                "How to break imports in multiline?", 1,
                new String[][] { { "Use escape char", ImportPreferences.BREAK_IMPORTS_MODE_ESCAPE },
                        { "Use parenthesis", ImportPreferences.BREAK_IMPORTS_MODE_PARENTHESIS } },
                p);
        addField(breakImportsInMultilineMode);

        updateEnablement(p, PyDevUiPrefs.getPreferenceStore().getString(ImportPreferences.IMPORT_ENGINE));
        Combo importEngineCombo = importEngineFieldEditor.getCombo();
        importEngineCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnablement(p, importEngineFieldEditor.getComboValue());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        addField(new LinkFieldEditor("link_saveactions",
                "\nNote: view <a>save actions</a> to automatically sort imports on save.", p,
                new SelectionListener() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String id = "org.python.pydev.editor.saveactions.PydevSaveActionsPrefPage";
                        IWorkbenchPreferenceContainer workbenchPreferenceContainer = ((IWorkbenchPreferenceContainer) getContainer());
                        workbenchPreferenceContainer.openPage(id, null);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                    }
                }));

        addField(new ScopedPreferencesFieldEditor(p, SharedCorePlugin.DEFAULT_PYDEV_PREFERENCES_SCOPE, this));
    }

    private void updateEnablement(Composite p, String importEngine) {
        boolean isIsort = importEngine.equals(ImportPreferences.IMPORT_ENGINE_ISORT);

        isortParameters.setVisible(isIsort, p);
        isortFileField.setVisible(isIsort);
        isortFormatterLocation.setVisible(isIsort, p);

        switch (importEngine) {
            case ImportPreferences.IMPORT_ENGINE_PEP_8:
                fromImportsFirstBooleanEditor.setVisible(true, p); // Setting only valid for PEP 8 engine.

                deleteUnusedImportsField.setVisible(true, p);
                groupImportsField.setVisible(true, p);
                multilineImportsField.setVisible(true, p);
                sortIndiviualOnGroupedField.setVisible(true, p);
                breakImportsInMultilineMode.setVisible(true, p);
                break;

            case ImportPreferences.IMPORT_ENGINE_REGULAR_SORT:
                fromImportsFirstBooleanEditor.setVisible(false, p);

                deleteUnusedImportsField.setVisible(true, p);
                groupImportsField.setVisible(true, p);
                multilineImportsField.setVisible(true, p);
                sortIndiviualOnGroupedField.setVisible(true, p);
                breakImportsInMultilineMode.setVisible(true, p);
                break;

            case ImportPreferences.IMPORT_ENGINE_ISORT:
                fromImportsFirstBooleanEditor.setVisible(false, p);
                deleteUnusedImportsField.setVisible(false, p);
                groupImportsField.setVisible(false, p);
                multilineImportsField.setVisible(false, p);
                sortIndiviualOnGroupedField.setVisible(false, p);
                breakImportsInMultilineMode.setVisible(false, p);
                break;
        }
        p.getParent().layout(true);
    }

    private void addFieldWithToolTip(BooleanFieldEditorCustom editor, Composite p, String tip) {
        addField(editor);
        editor.getDescriptionControl(p).setToolTipText(tip);
    }

    private void addFieldWithToolTip(ComboFieldEditor editor, Composite p, String tip) {
        addField(editor);
        editor.getLabelControl(p).setToolTipText(tip);
    }

    @Override
    public void init(IWorkbench workbench) {
        // pass
    }

}
