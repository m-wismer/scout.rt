/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.util.Arrays;
import java.util.Set;

public class UiTextContributor implements IUiTextContributor {

  @Override
  public void contributeUiTextKeys(Set<String> textKeys) {
    textKeys.addAll(Arrays.asList(
        // From org.eclipse.scout.rt.client
        "Remove",
        "ResetTableColumns",
        "ColumnSorting",
        "Column",
        "Cancel",
        "Ok",
        // From org.eclipse.scout.rt.ui.html
        "ui.CalendarToday",
        "ui.CalendarDay",
        "ui.CalendarWorkWeek",
        "ui.CalendarWeek",
        "ui.CalendarCalendarWeek",
        "ui.CalendarMonth",
        "ui.CalendarYear",
        "ui.BooleanColumnGroupingTrue",
        "ui.BooleanColumnGroupingFalse",
        "ui.InvalidDateFormat",
        "ui.EmptyCell",
        "ui.FilterBy_",
        "ui.SearchFor_",
        "ui.TableRowCount0",
        "ui.TableRowCount1",
        "ui.TableRowCount",
        "ui.NumRowsSelected",
        "ui.NumRowsSelectedMin",
        "ui.NumRowsFiltered",
        "ui.NumRowsFilteredBy",
        "ui.NumRowsFilteredMin",
        "ui.RemoveFilter",
        "ui.NumRowsLoaded",
        "ui.NumRowsLoadedMin",
        "ui.ReloadData",
        "ui.Reload",
        "ui.showEveryDate",
        "ui.groupedByWeekday",
        "ui.groupedByMonth",
        "ui.groupedByYear",
        "ui.OtherValues",
        "ui.Count",
        "ui.ConnectionInterrupted",
        "ui.ConnectionReestablished",
        "ui.Reconnecting_",
        "ui.SelectAll",
        "ui.SelectNone",
        "ui.ServerError",
        "ui.SessionTimeout",
        "ui.SessionExpiredMsg",
        "ui.Move",
        "ui.toBegin",
        "ui.forward",
        "ui.backward",
        "ui.toEnd",
        "ui.ascending",
        "ui.Copy",
        "ui.descending",
        "ui.ascendingAdditionally",
        "ui.descendingAdditionally",
        "ui.Sum",
        "ui.Total",
        "ui.overEverything",
        "ui.overSelection",
        "ui.grouped",
        "ui.Coloring",
        "ui.fromRedToGreen",
        "ui.fromGreenToRed",
        "ui.withBarChart",
        "ui.remove",
        "ui.add",
        "ui.FilterBy",
        "ui.Up",
        "ui.Continue",
        "ui.Ignore",
        "ui.ErrorCodeX",
        "ui.InternalUiErrorMsg",
        "ui.UiInconsistentMsg",
        "ui.UnexpectedProblem",
        "ui.InternalProcessingErrorMsg",
        "ui.PleaseWait_",
        "ui.ShowAllNodes",
        "ui.CW",
        "ui.ChooseFile",
        "ui.ChooseFiles",
        "ui.Upload",
        "ui.Browse",
        "ui.FromXToY",
        "ui.To",
        "ui.FileSizeLimitTitle",
        "ui.FileSizeLimit",
        "ui.ClipboardTimeoutTitle",
        "ui.ClipboardTimeout",
        "ui.PopupBlockerDetected",
        "ui.OpenManually",
        "ui.FileChooserHint",
        "ui.Outlines",
        "ui.NetworkError",
        "ui.Grouping",
        "ui.additionally",
        "ui.groupingApply",
        "ui.Average",
        "ui.Minimum",
        "ui.Maximum",
        "ui.Aggregation",
        "ui.LoadingPopupWindow"));
  }
}
