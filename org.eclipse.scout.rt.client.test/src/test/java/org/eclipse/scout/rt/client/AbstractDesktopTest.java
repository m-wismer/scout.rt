/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.client.AbstractDesktopTest.CheckSaveTestForm.MainBox.MessageField;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.testenvironment.ui.desktop.TestEnvironmentDesktop;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractDesktop}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractDesktopTest {
  private static final Object TEST_DATA_TYPE_1 = new Object();
  private static final Object TEST_DATA_TYPE_2 = new Object();

  private CheckSaveTestForm m_testForm;

  @Before
  public void setup() throws ProcessingException {
    m_testForm = new CheckSaveTestForm();
  }

  @After
  public void tearDown() throws ProcessingException {
    m_testForm.doClose();
  }

  @Test
  public void testNoSaveNeeded() throws ProcessingException {
    m_testForm.startNew();
    assertFalse(m_testForm.isSaveNeeded());
  }

  @Test
  public void testSaveNeeded() throws ProcessingException {
    m_testForm.startNew();
    m_testForm.getMessageField().setValue("test");
    assertTrue(m_testForm.isSaveNeeded());
  }

  /**
   * {@link AbstractDesktop#doBeforeClosingInternal()}
   */
  @Test
  public void testClosingDoBeforeClosingInternal() throws ProcessingException {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) ClientRunContexts.copyCurrent().desktop();

    boolean closing = desktop.doBeforeClosingInternal();
    assertTrue(closing);
  }

  @Test
  public void testUnsavedForms() throws ProcessingException {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) ClientRunContexts.copyCurrent().desktop();

    m_testForm.startNew();
    m_testForm.getMessageField().setValue("test");
    assertTrue(desktop.getUnsavedForms().contains(m_testForm));
  }

  @Test
  public void testDataChangedSimple() throws ProcessingException {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) ClientRunContexts.copyCurrent().desktop();

    final Holder<Object[]> resultHolder = new Holder<Object[]>(Object[].class);
    desktop.addDataChangeListener(new DataChangeListener() {

      @Override
      public void dataChanged(Object... dataTypes) throws ProcessingException {
        resultHolder.setValue(dataTypes);
      }
    }, TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    verifyDataChanged(resultHolder);
  }

  @Test
  public void testDataChangedChanging() throws ProcessingException {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) ClientRunContexts.copyCurrent().desktop();

    final Holder<Object[]> resultHolder = new Holder<Object[]>(Object[].class);
    desktop.addDataChangeListener(new DataChangeListener() {

      @Override
      public void dataChanged(Object... dataTypes) throws ProcessingException {
        resultHolder.setValue(dataTypes);
      }
    }, TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    desktop.setDataChanging(true);
    desktop.dataChanged(TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_1, TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_2, TEST_DATA_TYPE_2);
    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);
    desktop.dataChanged(TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_2);
    desktop.setDataChanging(false);
    verifyDataChanged(resultHolder);
  }

  @Test
  public void testGetDialogs() throws ProcessingException {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) ClientRunContexts.copyCurrent().desktop();

    //                       form
    //        _________________|___________________________
    //       |                 |                          |
    //     form_1            form_2                     form_3
    //                  _______|________________          |
    //                 |           |            |      form_3_1
    //               form_2_1    form_2_2    form_2_3
    //       __________|_____                   |
    //       |               |              form_2_3_1
    //  form_2_1_1        form_2_1_2
    //                       |
    //                    form_2_1_2_1

    P_Form form = new P_Form("form");
    form.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form.setDisplayParent(desktop);
    form.start();

    P_Form form_1 = new P_Form("form_1");
    form_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_1.setDisplayParent(form);
    form_1.start();

    P_Form form_2 = new P_Form("form_2");
    form_2.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2.setDisplayParent(form);
    form_2.start();

    P_Form form_3 = new P_Form("form_3");
    form_3.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_3.setDisplayParent(form);
    form_3.start();

    P_Form form_2_1 = new P_Form("form_2_1");
    form_2_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_1.setDisplayParent(form_2);
    form_2_1.start();

    P_Form form_2_2 = new P_Form("form_2_2");
    form_2_2.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_2.setDisplayParent(form_2);
    form_2_2.start();

    P_Form form_2_3 = new P_Form("form_2_3");
    form_2_3.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_3.setDisplayParent(form_2);
    form_2_3.start();

    P_Form form_2_3_1 = new P_Form("form_2_3_1");
    form_2_3_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_3_1.setDisplayParent(form_2_3);
    form_2_3_1.start();

    P_Form form_3_1 = new P_Form("form_3_1");
    form_3_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_3_1.setDisplayParent(form_3);
    form_3_1.start();

    P_Form form_2_1_1 = new P_Form("form_2_1_1");
    form_2_1_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_1_1.setDisplayParent(form_2_1);
    form_2_1_1.start();

    P_Form form_2_1_2 = new P_Form("form_2_1_2");
    form_2_1_2.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_1_2.setDisplayParent(form_2_1);
    form_2_1_2.start();

    P_Form form_2_1_2_1 = new P_Form("form_2_1_2_1");
    form_2_1_2_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_1_2_1.setDisplayParent(form_2_1_2);
    form_2_1_2_1.start();

    assertEquals(CollectionUtility.arrayList(form_1, form_2, form_3), desktop.getDialogs(form, false));
    assertEquals(CollectionUtility.arrayList(form_1, form_2_1_1, form_2_1_2_1, form_2_1_2, form_2_1, form_2_2, form_2_3_1, form_2_3, form_2, form_3_1, form_3), desktop.getDialogs(form, true));
  }

  protected void verifyDataChanged(Holder<Object[]> resultHolder) throws ProcessingException {
    Object[] result = resultHolder.getValue();
    assertTrue(result.length == 2);
    assertTrue(result[0] == TEST_DATA_TYPE_1 && result[1] == TEST_DATA_TYPE_2
        || result[0] == TEST_DATA_TYPE_2 && result[1] == TEST_DATA_TYPE_1);
  }

  @ClassId("d090cc19-ba7a-4f79-b147-e58765a837fb")
  class CheckSaveTestForm extends AbstractForm {

    /**
     * @throws ProcessingException
     */
    public CheckSaveTestForm() throws ProcessingException {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return TEXTS.get("AskIfSaveNeededForm");
    }

    public void startNew() throws ProcessingException {
      startInternal(new NewHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public MessageField getMessageField() {
      return getFieldByClass(MessageField.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class MessageField extends AbstractStringField {
      }

      @Order(20.0)
      public class OkButton extends AbstractOkButton {
      }

      @Order(30.0)
      public class CancelButton extends AbstractCancelButton {
      }
    }

    public class NewHandler extends AbstractFormHandler {
    }

  }

  private class P_Form extends AbstractForm {

    private String m_identifier;

    public P_Form(String identifier) throws ProcessingException {
      m_identifier = identifier;
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {
    }

    @Override
    public String toString() {
      return m_identifier;
    }
  }

}
