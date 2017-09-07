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
scout.SmartField2Multiline = function() {
  scout.SmartField2Multiline.parent.call(this);
  this.options;
  this._$multilineLines;
};
scout.inherits(scout.SmartField2Multiline, scout.SmartField2);

scout.SmartField2Multiline.prototype._render = function() {
  var $input, htmlComp;

  this.addContainer(this.$parent, 'smart-field has-icon', new scout.SmartField2Layout(this));
  this.addLabel();
  this.addFieldContainer(this.$parent.makeDiv('multiline'));
  htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.SmartField2MultilineLayout());

  $input = scout.fields.makeInputOrDiv(this, 'multiline-input')
    .on('mousedown', this._onFieldMouseDown.bind(this))
    .appendTo(this.$fieldContainer);

  if (!this.touch) {
    $input
      .blur(this._onFieldBlur.bind(this))
      .focus(this._onFieldFocus.bind(this))
      .keyup(this._onFieldKeyUp.bind(this))
      .keydown(this._onFieldKeyDown.bind(this))
      .on('input', this._onInputChanged.bind(this));
  }
  this.addField($input);
  this._$multilineLines = this.$fieldContainer
    .appendDiv('multiline-lines')
    .on('click', this._onMultilineLinesClick.bind(this));
  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon(this.$fieldContainer);
  this.$icon.addClass('needsclick');
  this.addClearIcon(this.$fieldContainer);
  this.addStatus();
};

/**
 * Sets the focus to the input field when user clicks on text lines, but only if nothing is selected.
 * Otherwise it would be impossible for the user to select the text. That's why we cannot use the
 * mousedown event here too.
 */
scout.SmartField2Multiline.prototype._onMultilineLinesClick = function(event) {
  if (this.enabled) {
    var selection = this.$field.window(true).getSelection();
    if (!selection.toString()) {
      this.$field.focus();
    }
  }
};

scout.SmartField2Multiline.prototype._renderDisplayText = function() {
  scout.SmartField2Multiline.parent.prototype._renderDisplayText.call(this);
  var additionalLines = this.additionalLines();
  if (additionalLines) {
    this._$multilineLines.html(scout.arrays.formatEncoded(additionalLines, '<br/>'));
  } else {
    this._$multilineLines.empty();
  }
};

scout.SmartField2Multiline.prototype._getInputBounds = function() {
  var fieldBounds = scout.graphics.offsetBounds(this.$fieldContainer),
    textFieldBounds = scout.graphics.offsetBounds(this.$field);
  fieldBounds.height = textFieldBounds.height;
  return fieldBounds;
};

scout.SmartField2Multiline.prototype._renderFocused = function() {
  scout.SmartField2Multiline.parent.prototype._renderFocused.call(this);
  this._$multilineLines.toggleClass('focused', this.focused);
};

scout.SmartField2Multiline.prototype._updateErrorStatusClasses = function(statusClass, hasStatus) {
  scout.SmartField2Multiline.parent.prototype._updateErrorStatusClasses.call(this, statusClass, hasStatus);
  this._$multilineLines.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
  this._$multilineLines.addClass(statusClass, hasStatus);
};
