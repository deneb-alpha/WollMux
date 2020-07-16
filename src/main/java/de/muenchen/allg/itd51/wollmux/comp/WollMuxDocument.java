/*-
 * #%L
 * WollMux
 * %%
 * Copyright (C) 2005 - 2020 Landeshauptstadt München
 * %%
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * #L%
 */
package de.muenchen.allg.itd51.wollmux.comp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.star.beans.PropertyValue;
import com.sun.star.text.XTextDocument;

import de.muenchen.allg.afid.UnoProps;
import de.muenchen.allg.itd51.wollmux.SyncActionListener;
import de.muenchen.allg.itd51.wollmux.XWollMuxDocument;
import de.muenchen.allg.itd51.wollmux.document.DocumentManager;
import de.muenchen.allg.itd51.wollmux.document.TextDocumentModel;
import de.muenchen.allg.itd51.wollmux.event.handlers.OnManagePrintFunction;
import de.muenchen.allg.itd51.wollmux.event.handlers.OnSetFormValue;
import de.muenchen.allg.itd51.wollmux.event.handlers.OnSetInsertValues;

/**
 * Implementiert XWollMuxDocument für alle dokumentspezifischen Aktionen
 *
 * @author Christoph Lutz (D-III-ITD-D101)
 */
public class WollMuxDocument implements XWollMuxDocument
{
  private XTextDocument doc;

  private HashMap<String, String> mapDbSpalteToValue;

  public WollMuxDocument(XTextDocument doc)
  {
    this.doc = doc;
    this.mapDbSpalteToValue = new HashMap<String, String>();
  }

  /**
   * Nimmt die Druckfunktion functionName in die Liste der Druckfunktionen des Dokuments auf. Die
   * Druckfunktion wird dabei automatisch aktiv, wenn das Dokument das nächste mal mit
   * Datei-&gt;Drucken gedruckt werden soll. Ist die Druckfunktion bereits in der Liste der
   * Druckfunktionen des Dokuments enthalten, so geschieht nichts.
   *
   * Hinweis: Die Ausführung erfolgt asynchron, d.h. addPrintFunction() kehrt unter Umständen
   * bereits zurück BEVOR die Methode ihre Wirkung entfaltet hat.
   *
   * @param functionName
   *          der Name einer Druckfunktion, die im Abschnitt "Druckfunktionen" der
   *          WollMux-Konfiguration definiert sein muss.
   */
  @Override
  public void addPrintFunction(String functionName)
  {
    new OnManagePrintFunction(doc, functionName, false).emit();
  }

  /**
   * Löscht die Druckfunktion functionName aus der Liste der Druckfunktionen des Dokuments. Die
   * Druckfunktion wird damit ab dem nächsten Aufruf von Datei-&gt;Drucken nicht mehr aufgerufen.
   * Ist die Druckfunktion nicht in der Liste der Druckfunktionen des Dokuments enthalten, so
   * geschieht nichts.
   *
   * Hinweis: Die Ausführung erfolgt asynchron, d.h. removePrintFunction() kehrt unter Umständen
   * bereits zurück BEVOR die Methode ihre Wirkung entfaltet hat.
   *
   * @param functionName
   *          der Name einer Druckfunktion, die im Dokument gesetzt ist.
   */
  @Override
  public void removePrintFunction(String functionName)
  {
    new OnManagePrintFunction(doc, functionName, true).emit();
  }

  /**
   * Setzt den Wert mit ID id in der FormularGUI auf Wert mit allen Folgen, die das
   * nach sich zieht (PLAUSIs, AUTOFILLs, Ein-/Ausblendungen,...). Es ist nicht
   * garantiert, dass der Befehl ausgeführt wird, bevor updateFormGUI() aufgerufen
   * wurde. Eine Implementierung mit einer Queue ist möglich.
   *
   * Anmerkung: Eine Liste aller verfügbaren IDs kann über die Methode
   * XWollMuxDocument.getFormValues() gewonnen werden.
   *
   * @param id
   *          ID zu der der neue Formularwert gesetzt werden soll.
   * @param value
   *          Der neu zu setzende Formularwert.
   */
  @Override
  public void setFormValue(String id, String value)
  {
    SyncActionListener s = new SyncActionListener();
    new OnSetFormValue(doc, id, value, s).emit();
    s.synchronize();
  }

  /**
   * Setzt den Wert, der bei insertValue-Dokumentkommandos mit DB_SPALTE "dbSpalte"
   * eingefügt werden soll auf Wert. Es ist nicht garantiert, dass der neue Wert im
   * Dokument sichtbar wird, bevor updateInsertFields() aufgerufen wurde. Eine
   * Implementierung mit einer Queue ist möglich.
   *
   * Anmerkung: Eine Liste aller verfügbaren DB_SPALTEn kann mit der Methode
   * XWollMux.getInsertValues() gewonnen werden.
   *
   * @param dbSpalte
   *          enthält den Namen der Absenderdatenspalte, deren Wert geändert werden
   *          soll.
   * @param value
   *          enthält den neuen Wert für dbSpalte.
   */
  @Override
  public void setInsertValue(String dbSpalte, String value)
  {
    mapDbSpalteToValue.put(dbSpalte, value);
  }

  /**
   * Sorgt für die Ausführung aller noch nicht ausgeführten setFormValue()
   * Kommandos. Die Methode kehrt garantiert erst zurück, wenn alle
   * setFormValue()-Kommandos ihre Wirkung im WollMux und im entsprechenden
   * Dokument entfaltet haben.
   */
  @Override
  public void updateFormGUI()
  {
  // Wird implementiert, wenn setFormValue(...) so umgestellt werden soll, dass die
  // Änderungen vorerst nur in einer queue gesammelt werden und mit dieser Methode
  // aktiv werden sollen.
  }

  /**
   * Sorgt für die Ausführung aller noch nicht ausgeführten setInsertValue()
   * Kommandos. Die Methode kehrt garantiert erst zurück, wenn alle
   * setInsertValue()-Kommandos ihre Wirkung im WollMux und im entsprechenden
   * Dokument entfaltet haben.
   */
  @Override
  public void updateInsertFields()
  {
    Map<String, String> m = new HashMap<>(mapDbSpalteToValue);
    mapDbSpalteToValue.clear();
    SyncActionListener s = new SyncActionListener();
    new OnSetInsertValues(doc, m, s).emit();
    s.synchronize();
  }

  /**
   * Liefert die zum aktuellen Zeitpunkt gesetzten Formularwerte dieses WollMux-Dokuments in einem
   * Array von PropertyValue-Objekten zurück. Dabei repräsentieren die Attribute
   * {@link PropertyValue#Name} die verfügbaren IDs und die Attribute {@link PropertyValue#Value}
   * die zu ID zugehörigen Formularwerte.
   *
   * Jeder Aufruf erzeugt ein komplett neues und unabhängiges Objekt mit allen Einträgen die zu dem
   * Zeitpunkt gültig sind. Eine Änderung der Werte des Rückgabeobjekts hat daher keine Auswirkung
   * auf den WollMux.
   *
   * @return Array von PropertyValue-Objekten mit den aktuell gesetzten Formularwerten dieses
   *         WollMux-Dokuments. Gibt es keine Formularwerte im Dokument, so ist das Array leer (aber
   *         != null).
   */
  @Override
  public PropertyValue[] getFormValues()
  {
    UnoProps p = new UnoProps();
    TextDocumentModel model =
      DocumentManager.getTextDocumentController(doc).getModel();
    Map<String, String> id2value = model.getFormFieldValuesMap();
    for (Entry<String, String> e : id2value.entrySet())
      if (e.getValue() != null) p.setPropertyValue(e.getKey(), e.getValue());
    return p.getProps();
  }
}