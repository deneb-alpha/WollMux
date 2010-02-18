/*
 * Dateiname: InsertionModel.java
 * Projekt  : WollMux
 * Funktion : Stellt eine Einfügestelle im Dokument dar (z.B. insertValue).
 * 
 * Copyright (c) 2008 Landeshauptstadt München
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the European Union Public Licence (EUPL), 
 * version 1.0 (or any later version).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 *
 * You should have received a copy of the European Union Public Licence
 * along with this program. If not, see 
 * http://ec.europa.eu/idabc/en/document/7330
 *
 * Änderungshistorie:
 * Datum      | Wer | Änderungsgrund
 * -------------------------------------------------------------------
 * 27.06.2008 | BNK | Erstellung
 * -------------------------------------------------------------------
 *
 * @author Matthias Benkmann (D-III-ITD D.10)
 * @version 1.0
 * 
 */
package de.muenchen.allg.itd51.wollmux.former.insertion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.muenchen.allg.itd51.parser.ConfigThingy;
import de.muenchen.allg.itd51.wollmux.former.FormularMax4000;
import de.muenchen.allg.itd51.wollmux.former.function.FunctionSelection;
import de.muenchen.allg.itd51.wollmux.former.function.FunctionSelectionAccess;
import de.muenchen.allg.itd51.wollmux.former.function.ParamValue;

public abstract class InsertionModel
{
  protected static final String FM4000AUTO_GENERATED_TRAFO =
    "FM4000AutoGeneratedTrafo";

  /**
   * Die TRAFO für diese Einfügung.
   */
  protected FunctionSelection trafo;

  /**
   * Der FormularMax4000 zu dem dieses Model gehört.
   */
  protected FormularMax4000 formularMax4000;

  /**
   * Die {@link ModelChangeListener}, die über Änderungen dieses Models informiert
   * werden wollen.
   */
  private List<ModelChangeListener> listeners = new Vector<ModelChangeListener>(1);

  /**
   * Entfernt die Einfügestelle komplett aus dem Dokument, d,h, sowohl das eventuell
   * vorhandene WollMux-Bookmark als auch den Feldbefehl.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public abstract void removeFromDocument();

  /**
   * Liefert den "Namen" der Einfügestelle. Dies kann z.B. der Name des Bookmarks
   * sein, das die Einfügestelle umschließt.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public abstract String getName();

  /**
   * Setzt den ViewCursor auf die Einfügestelle.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public abstract void selectWithViewCursor();

  /**
   * Lässt dieses {@link InsertionModel} sein zugehöriges Bookmark bzw, sonstige
   * Daten updaten, die die TRAFO betreffen.
   * 
   * @param mapFunctionNameToConfigThingy
   *          bildet einen Funktionsnamen auf ein ConfigThingy ab, dessen Wurzel der
   *          Funktionsname ist und dessen Inhalt eine Funktionsdefinition. Wenn
   *          diese Einfügung mit einer TRAFO versehen ist, wird für das
   *          Aktualisieren des Bookmarks ein Funktionsname generiert, der noch nicht
   *          in dieser Map vorkommt und ein Mapping für diese Funktion wird in die
   *          Map eingefügt.
   * @return false, wenn ein update nicht möglich ist. In dem Fall wird das
   *         entsprechende Bookmark entfernt und dieses InsertionModel sollte nicht
   *         weiter verwendet werden.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public abstract boolean updateDocument(
      Map<String, ConfigThingy> mapFunctionNameToConfigThingy);

  /**
   * Liefert den FormularMax4000 zu dem dieses Model gehört.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public FormularMax4000 getFormularMax4000()
  {
    return formularMax4000;
  }

  /**
   * Setzt die TRAFO auf trafo, wobei das Objekt direkt übernommen (nicht kopiert)
   * wird. ACHTUNG! Derzeit verständigt diese Funktion keine ModelChangeListener,
   * d.h. Änderungen an diesem Attribut werden nicht im FM4000 propagiert. Diese
   * Funktion kann also derzeit nur sinnvoll auf einem frischen InsertionModel
   * verwendet werden, bevor es zur insertionModelList hinzugefügt wird.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public void setTrafo(FunctionSelection trafo)
  {
    this.trafo = trafo;
    formularMax4000.documentNeedsUpdating();
  }

  /**
   * Liefert true gdw dieses InsertionModel eine TRAFO gesetzt hat.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public boolean hasTrafo()
  {
    return !trafo.isNone();
  }

  /**
   * Liefert ein Interface zum Zugriff auf die TRAFO dieses Objekts.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1) TESTED
   */
  public FunctionSelectionAccess getTrafoAccess()
  {
    return new MyTrafoAccess();
  }

  /**
   * listener wird über Änderungen des Models informiert.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public void addListener(ModelChangeListener listener)
  {
    if (!listeners.contains(listener)) listeners.add(listener);
  }

  /**
   * Benachrichtigt alle auf diesem Model registrierten Listener, dass das Model aus
   * seinem Container entfernt wurde. ACHTUNG! Darf nur von einem entsprechenden
   * Container aufgerufen werden, der das Model enthält.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1) TESTED
   */
  public void hasBeenRemoved()
  {
    Iterator<ModelChangeListener> iter = listeners.iterator();
    while (iter.hasNext())
    {
      ModelChangeListener listener = iter.next();
      listener.modelRemoved(this);
    }
    formularMax4000.documentNeedsUpdating();
  }

  /**
   * Ruft für jeden auf diesem Model registrierten {@link ModelChangeListener} die
   * Methode
   * {@link ModelChangeListener#attributeChanged(InsertionModel, int, Object)} auf.
   */
  protected void notifyListeners(int attributeId, Object newValue)
  {
    Iterator<ModelChangeListener> iter = listeners.iterator();
    while (iter.hasNext())
    {
      ModelChangeListener listener = iter.next();
      listener.attributeChanged(this, attributeId, newValue);
    }
    formularMax4000.documentNeedsUpdating();
  }

  /**
   * Diese Klasse leitet Zugriffe weiter an das Objekt {@link InsertionModel#trafo}.
   * Bei ändernden Zugriffen wird auch noch der FormularMax4000 benachrichtigt, dass
   * das Dokument geupdatet werden muss. Im Prinzip müsste korrekterweise ein
   * ändernder Zugriff auf trafo auch einen Event an die ModelChangeListener
   * schicken. Allerdings ist dies derzeit nicht implementiert, weil es derzeit genau
   * eine View gibt für die Trafo, so dass konkurrierende Änderungen gar nicht
   * möglich sind.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  private class MyTrafoAccess implements FunctionSelectionAccess
  {
    public boolean isReference()
    {
      return trafo.isReference();
    }

    public boolean isExpert()
    {
      return trafo.isExpert();
    }

    public boolean isNone()
    {
      return trafo.isNone();
    }

    public String getFunctionName()
    {
      return trafo.getFunctionName();
    }

    public ConfigThingy getExpertFunction()
    {
      return trafo.getExpertFunction();
    }

    public void setParameterValues(Map<String, ParamValue> mapNameToParamValue)
    {
      trafo.setParameterValues(mapNameToParamValue);
      formularMax4000.documentNeedsUpdating();
    }

    public void setFunction(String functionName, String[] paramNames)
    {
      trafo.setFunction(functionName, paramNames);
      formularMax4000.documentNeedsUpdating();
    }

    public void setExpertFunction(ConfigThingy funConf)
    {
      trafo.setExpertFunction(funConf);
      formularMax4000.documentNeedsUpdating();
    }

    public void setParameterValue(String paramName, ParamValue paramValue)
    {
      trafo.setParameterValue(paramName, paramValue);
      formularMax4000.documentNeedsUpdating();
    }

    public String[] getParameterNames()
    {
      return trafo.getParameterNames();
    }

    public boolean hasSpecifiedParameters()
    {
      return trafo.hasSpecifiedParameters();
    }

    public ParamValue getParameterValue(String paramName)
    {
      return trafo.getParameterValue(paramName);
    }
  }

  /**
   * Interface für Listener, die über Änderungen eines Models informiert werden
   * wollen.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public static interface ModelChangeListener
  {
    /**
     * Wird aufgerufen wenn ein Attribut des Models sich geändert hat.
     * 
     * @param model
     *          das InsertionModel, das sich geändert hat.
     * @param attributeId
     *          eine der {@link InsertionModel#ID_ATTR Attribut-ID-Konstanten}.
     * @param newValue
     *          der neue Wert des Attributs. Numerische Attribute werden als Integer
     *          übergeben.
     * @author Matthias Benkmann (D-III-ITD 5.1)
     */
    public void attributeChanged(InsertionModel model, int attributeId,
        Object newValue);

    /**
     * Wird aufgerufen, wenn model aus seinem Container entfernt wird (und damit in
     * keiner View mehr angezeigt werden soll).
     * 
     * @author Matthias Benkmann (D-III-ITD 5.1)
     */
    public void modelRemoved(InsertionModel model);
  }

}
