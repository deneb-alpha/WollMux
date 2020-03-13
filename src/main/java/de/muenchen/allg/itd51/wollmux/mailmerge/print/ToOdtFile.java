package de.muenchen.allg.itd51.wollmux.mailmerge.print;

import javax.print.PrintException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.text.MailMergeType;

import de.muenchen.allg.itd51.wollmux.XPrintModel;
import de.muenchen.allg.itd51.wollmux.core.util.L;
import de.muenchen.allg.itd51.wollmux.dialog.InfoDialog;
import de.muenchen.allg.itd51.wollmux.func.print.PrintFunction;

/**
 * Print function for mailmerge by LibreOffice mailmerge with type {@link MailMergeType#SHELL}.
 */
public class ToOdtFile extends PrintFunction
{

  private static final Logger LOGGER = LoggerFactory.getLogger(ToOdtFile.class);

  /**
   * A {@link PrintFunction} with name "OOoMailMergeToOdtFile" and order 200.
   */
  public ToOdtFile()
  {
    super("OOoMailMergeToOdtFile", 200);
  }

  @Override
  public void print(XPrintModel printModel)
  {
    try (OOoBasedMailMerge mailMerge = new OOoBasedMailMerge(printModel, MailMergeType.SHELL))
    {
      mailMerge.doMailMerge();
    } catch (PrintException e)
    {
      LOGGER.error("Fehler beim Seriendruck", e);
      printModel.cancel();
      InfoDialog.showInfoModal(L.m("WollMux-Seriendruck"), L.m(e.getMessage()));
    } catch (Exception ex)
    {
      LOGGER.error("Fehler beim Aufräumen der temporären Dokumente", ex);
    }
  }
}
