/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, IconDesc, Image, scout, Widget} from '../index';
import $ from 'jquery';

/**
 * Widget representing an icon. It may be a font icon or an image icon. Depending on the type, either a span or an img tag will be rendered.
 * <p>
 * See also jquery-scout.icon/appendIcon. Main difference to these implementations is that the image loading will invalidate the layout by using {@link Image}.
 */
export default class Icon extends Widget {

  constructor() {
    super();

    this.autoFit = false;
    /** @type {IconDesc} */
    this.iconDesc = null;

    /**
     * Is set if the icon is rendered and an image, it is not set if it is a font icon
     * @type Image
     */
    this.image = null;
    this.prepend = false;
    this._svg = null;
    this.inlineSvgLoading = false;
  }

  _init(model) {
    super._init(model);
    this._setIconDesc(this.iconDesc);
  }

  _render() {
    this._renderIconDesc(); // Must not be in _renderProperties because it creates $container -> properties like visible etc. need to be rendered afterwards
    this._renderInlineSvgLoading();
  }

  /**
   * Accepts either an iconId as string or an {@link IconDesc}.
   * @param {(string|IconDesc)} icon
   */
  setIconDesc(iconDesc) {
    this.setProperty('iconDesc', iconDesc);
  }

  _setIconDesc(iconDesc) {
    iconDesc = IconDesc.ensure(iconDesc);
    this._setProperty('iconDesc', iconDesc);

    // Clear cache
    this._svg = null;
  }

  _renderIconDesc() {
    this._removeContainer(); // Also removes inline svg
    this._removeImageIcon();

    if (!this.iconDesc || this.iconDesc.isFontIcon()) {
      this._renderFontIcon();
    } else if (this.iconDesc.isInlineSvg()) {
      this._renderInlineSvgIcon();
    } else {
      this._renderImageIcon();
    }
    if (!this.rendering) {
      this._renderProperties();
    }
    this.invalidateLayoutTree();
  }

  _renderInlineSvgIcon() {
    if (!this._svg) {
      this._loadSvg().then(data => this._onInlineSvgLoad(data));
      this.setInlineSvgLoading(true);
      return;
    }
    // TODO CGU add to jquery-scout? maybe it should support inline-svg:<svg> and do the prepare and append while icon.js does caching
    this.$container = this._prepareSvg(this._svg);
    this.$container.appendTo(this.$parent);
    if (this.prepend) {
      this.$container.prependTo(this.$parent);
    }
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  _onInlineSvgLoad(data) {
    this._svg = data;
    this.setInlineSvgLoading(false);
    if (this.rendered) {
      this._renderInlineSvgIcon();
      this._renderProperties();
      this.invalidateLayoutTree();
    }
  }

  setInlineSvgLoading(loading) {
    this.setProperty('inlineSvgLoading', loading);
  }

  _renderInlineSvgLoading() {
    if (!this.iconDesc || !this.iconDesc.isInlineSvg()) {
      return;
    }
    if (this.inlineSvgLoading) {
      if (!this.$container) {
        this.$container = this.$parent.appendDiv('loading icon svg-icon');
        if (this.prepend) {
          this.$container.prependTo(this.$parent);
        }
        this.htmlComp = HtmlComponent.install(this.$container, this.session);
      }
    } else {
      if (this.$container && this.$container.hasClass('loading')) {
        this._removeContainer();
      }
    }
  }

  _prepareSvg(svg) {
    return $(svg)
      .filter('svg') // ignore XML declaration and comments
      .addClass('svg-icon icon');
  }

  _loadSvg() {
    return $.ajax({
      dataType: 'text',
      url: this.iconDesc.iconUrl
    });
  }

  _renderFontIcon() {
    this.$container = this.$parent.appendIcon(this.iconDesc);
    if (this.prepend) {
      this.$container.prependTo(this.$parent);
    }

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  _removeContainer() {
    if (this.$container) {
      this.$container.remove();
      this.$container = null;
    }
  }

  _renderImageIcon() {
    if (this.image) {
      return;
    }
    this.image = scout.create('Image', {
      parent: this,
      imageUrl: this.iconDesc.iconUrl,
      cssClass: 'icon image-icon',
      autoFit: this.autoFit,
      prepend: this.prepend
    });
    this.image.render(this.$parent);
    this.image.one('destroy', () => {
      this.image = null;
    });
    this.image.on('load error', event => {
      // propagate event
      this.trigger(event.type, event);
    });
    this.$container = this.image.$container;
    this.htmlComp = this.image.htmlComp;
  }

  _removeImageIcon() {
    if (this.image) {
      this.image.destroy();
      this.image = null;
    }
  }

  /**
   * Delegates to this.image.setAutoFit, but only if Icon is an image. Beside updating the autoFit property, this method has no effect if the icon is a font-icon.
   */
  setAutoFit(autoFit) {
    this.setProperty('autoFit', autoFit);
    if (this.image) {
      this.image.setAutoFit(autoFit);
    }
  }
}
