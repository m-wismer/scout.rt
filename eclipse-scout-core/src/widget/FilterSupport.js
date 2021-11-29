/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, FormField, HAlign, keys, KeyStroke, objects, scout, strings, styles, ValueField, WidgetSupport} from '../index';
import FocusFilterFieldKeyStroke from '../keystroke/FocusFilterFieldKeyStroke';

export default class FilterSupport extends WidgetSupport {
  /**
   * @typedef {WidgetSupportOptions} FilterSupportOptions
   * @property {Function} filterElements Filter all elements.
   * @property {Function} getElementsForFiltering Get all elements to which the filters should be applied.
   * @property {Function} getElementText Get text of an element.
   * @property {Function} createTextFilter Create a text filter.
   * @property {Function} updateTextFilterText Update the text on the filter, this is mandatory if createTextFilter is set.
   */

  /**
   * @param {FilterSupportOptions} options a mandatory options object
   */
  constructor(options) {
    super(options);

    if (options.filterElements) {
      this._filterElements = options.filterElements;
    } else {
      this._filterElements = this._filter.bind(this);
      scout.assertParameter('getElementsForFiltering', options.getElementsForFiltering);
      this._getElementsForFiltering = options.getElementsForFiltering;
    }
    this._getElementText = options.getElementText || ((element) => $(element).text());

    if (options.createTextFilter) {
      scout.assertParameter('updateTextFilterText', options.updateTextFilterText);
      this._createTextFilter = options.createTextFilter;
      this._updateTextFilterText = options.updateTextFilterText;
    } else {
      this._createTextFilter = this._createDefaultTextFilter.bind(this);
      this._updateTextFilterText = this._updateDefaultTextFilterText.bind(this);
    }

    this._filterField = null;

    this._filterFieldDisplayTextChangedHandler = this._onFilterFieldDisplayTextChanged.bind(this);
    this._blurHandler = this._onBlur.bind(this);

    this._focusFilterFieldKeyStroke = null;
    this._cancelFilterFieldKeyStroke = null;
    this._exitFilterFieldKeyStroke = null;

    this._textFilter = null;
  }

  _createDefaultTextFilter() {
    return {
      acceptedText: null,
      accept: element => {
        if (strings.empty(this._textFilter.acceptedText) || !this.widget.isTextFilterFieldVisible()) {
          return true;
        }
        let text = this._getElementText(element);
        if (strings.empty(text)) {
          return false;
        }
        return strings.contains(text.toLowerCase(), this._textFilter.acceptedText.toLowerCase());
      }
    };
  }

  _updateDefaultTextFilterText(filter, text) {
    if (objects.equals(filter.acceptedText, text)) {
      return false;
    }
    filter.acceptedText = text;
    return true;
  }

  renderFilterField() {
    if (this.widget.isTextFilterFieldVisible()) {
      this._renderFilterField();
    } else {
      this._removeFilterField();
    }
  }

  _renderFilterField() {
    this._ensure$Container();
    this._filterField = scout.create('StringField', {
      parent: this.widget,
      label: this.widget.session.text('ui.Filter'),
      labelVisible: false,
      cssClass: 'filter-field',
      fieldStyle: FormField.FieldStyle.CLASSIC,
      statusVisible: false,
      clearable: ValueField.Clearable.ALWAYS,
      updateDisplayTextOnModify: true,
      preventInitialFocus: true
    });
    this._filterField.render(this.$container);

    this._filterField.$field.attr('tabIndex', -1);
    this._filterField.$container.toggleClass('empty', !this._filterField.displayText);

    let color = styles.getFirstOpaqueBackgroundColor(this._filterField.$container),
      transparentColorRgba = $.extend(true, {}, styles.rgb(color), {alpha: 0.5}),
      transparentColor = 'rgba(' + transparentColorRgba.red + ', ' + transparentColorRgba.green + ', ' + transparentColorRgba.blue + ', ' + transparentColorRgba.alpha + ')';
    this._filterField.$container.css('--filter-field-background-color', color);
    this._filterField.$container.css('--filter-field-transparent-background-color', transparentColor);

    this._textFilter = this._createTextFilter();
    this.addFilter(this._textFilter);

    this._filterField.on('propertyChange:displayText', this._filterFieldDisplayTextChangedHandler);
    this._filterField.$field.on('blur', this._blurHandler);
    this.$container.on('blur', this._blurHandler);

    this.widget.$container.data('filter-field', this._filterField.$field);
    this._focusFilterFieldKeyStroke = new FocusFilterFieldKeyStroke(this.widget);
    this.widget.keyStrokeContext.registerKeyStroke(this._focusFilterFieldKeyStroke);

    // TODO fsh maybe move to own class
    this._cancelFilterFieldKeyStroke = new KeyStroke();
    this._cancelFilterFieldKeyStroke.field = this._filterField;
    this._cancelFilterFieldKeyStroke.which = [keys.ESC];
    this._cancelFilterFieldKeyStroke.stopPropagation = true;
    this._cancelFilterFieldKeyStroke.renderingHints.hAlign = HAlign.RIGHT;
    this._cancelFilterFieldKeyStroke.handle = this._cancelFilterField.bind(this);

    this._filterField.keyStrokeContext.registerKeyStroke(this._cancelFilterFieldKeyStroke);

    this._exitFilterFieldKeyStroke = new KeyStroke();
    this._exitFilterFieldKeyStroke.field = this._filterField;
    this._exitFilterFieldKeyStroke.which = [keys.ENTER];
    this._exitFilterFieldKeyStroke.stopPropagation = true;
    this._exitFilterFieldKeyStroke.renderingHints.hAlign = HAlign.RIGHT;
    this._exitFilterFieldKeyStroke.handle = this._exitFilterField.bind(this);

    this._filterField.keyStrokeContext.registerKeyStroke(this._exitFilterFieldKeyStroke);
  }

  _onFilterFieldDisplayTextChanged(event) {
    this._filterField.$container.toggleClass('empty', !event.newValue);
    if (!this._updateTextFilterText(this._textFilter, event.newValue)) {
      return;
    }
    this.filter();
  }

  _onBlur(event) {
    if (!this.$container.isOrHas(event.relatedTarget)) {
      this._resetFilterField();
    }
  }

  _exitFilterField() {
    this.widget.focus();
  }

  _cancelFilterField() {
    this._resetFilterField();
    this._exitFilterField();
  }

  _resetFilterField() {
    this._filterField.setValue(null);
  }

  _removeFilterField() {
    if (this._filterField) {
      this._resetFilterField();
      this._filterField.remove();
      this._filterField = null;

      this.removeFilter(this._textFilter);
      this._textFilter = null;

      this.$container.off('blur', this._blurHandler);

      this.widget.keyStrokeContext.unregisterKeyStroke(this._focusFilterFieldKeyStroke);
      this._focusFilterFieldKeyStroke = null;
    }
  }

  remove() {
    this._removeFilterField();
  }

  addFilter(filter, applyFilter) {
    let filtersToAdd = arrays.ensure(filter);
    let filters = this._getFilters().slice(),
      oldFilters = filters.slice();
    filtersToAdd.forEach(f => {
      if (this._hasFilter(filters, f)) {
        return;
      }
      filters.push(f);
    });
    if (filters.length === this._getFilters().length) {
      return;
    }
    this._setFilters(filters, applyFilter);

    let newFilters = this._getFilters().slice();
    arrays.removeAll(newFilters, oldFilters);

    return newFilters;
  }

  removeFilter(filter, applyFilter) {
    let filtersToRemove = arrays.ensure(filter);
    let filters = this._getFilters().slice(),
      oldFilters = filters.slice();

    let changed = false;
    filtersToRemove.forEach(f => {
      if (objects.isFunction(f)) {
        f = this._getFilterCreatedByFunction(filters, f);
      }
      changed = arrays.remove(filters, f) || changed;
    });

    if (!changed) {
      return;
    }
    this._setFilters(filters, applyFilter);

    let newFilters = this._getFilters().slice();
    arrays.removeAll(oldFilters, newFilters);

    return oldFilters;
  }

  setFilters(filters, applyFilter) {
    filters = arrays.ensure(filters);
    this._addSyntheticFilters(filters);

    if (this._getFilters().length === filters && !filters.some(filter => !this._hasFilter(this._getFilters(), filter))) {
      return;
    }

    this._setFilters(filters, applyFilter);
  }

  _setFilters(filters, applyFilter) {
    this.widget.setProperty('filters', filters.map(filter => {
      if (objects.isFunction(filter)) {
        return this._createFilterByFunction(filter);
      }
      return filter;
    }));
    if (applyFilter) {
      this.filter();
    }
  }

  _addSyntheticFilters(filters) {
    this._addSyntheticFilter(filters, this._textFilter);
  }

  _addSyntheticFilter(filters, syntheticFilter) {
    if (!syntheticFilter || this._hasFilter(filters, syntheticFilter)) {
      return;
    }
    filters.push(syntheticFilter);
  }

  _getFilters() {
    return this.widget.filters;
  }

  _hasFilter(filters, filter) {
    if (objects.isFunction(filter)) {
      return !!this._getFilterCreatedByFunction(filters, filter);
    }
    return filters.indexOf(filter) > -1;
  }

  _getFilterCreatedByFunction(filters, filterFunc) {
    return arrays.find(filters, filter => filter.createdByFunction && filter.accept === filterFunc);
  }

  _createFilterByFunction(filterFunc) {
    return {
      createdByFunction: true,
      accept: filterFunc
    };
  }

  filter() {
    return this._filterElements();
  }

  _filter() {
    return this.applyFilters(this._getElementsForFiltering(), true);
  }

  applyFilters(elements, fullReset) {
    if (this._getFilters().length === 0 && !scout.nvl(fullReset, false)) {
      return;
    }
    let newlyShown = [];
    let newlyHidden = [];
    let changed = false;
    elements.forEach(element => {
      if (this.applyFiltersForElement(element)) {
        changed = true;
        if (element.filterAccepted) {
          newlyShown.push(element);
        } else {
          newlyHidden.push(element);
        }
      }
    });

    if (changed) {
      this.widget.filteredElementsDirty = true;
      this.widget.updateFilteredElements();
    }

    return {
      newlyHidden: newlyHidden,
      newlyShown: newlyShown
    };
  }

  applyFiltersForElement(element) {
    if (this._elementAcceptedByFilters(element)) {
      if (!element.filterAccepted) {
        element.setFilterAccepted(true);
        return true;
      }
    } else if (element.filterAccepted) {
      element.setFilterAccepted(false);
      return true;
    }
    return false;
  }

  _elementAcceptedByFilters(element) {
    return !this._getFilters().some(filter => !filter.accept(element));
  }
}
