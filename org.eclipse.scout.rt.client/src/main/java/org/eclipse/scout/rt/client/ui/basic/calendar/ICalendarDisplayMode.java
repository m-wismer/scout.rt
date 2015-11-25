/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.calendar;

/**
 * Interface providing display-modes for calender-like components like calendar and planner.
 */
public interface ICalendarDisplayMode {

  // never change final constants (properties files might have references)
  // these constants should correspond with those from the planner
  int INTRADAY = 0;
  int DAY = 1;
  int WEEK = 2;
  int MONTH = 3;
  int WORK_WEEK = 4;
  int CALENDAR_WEEK = 5;
  int YEAR = 6;
}
