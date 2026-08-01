package de.ruu.app.pragma.fx;

import de.ruu.lib.fx.control.dialog.ExceptionDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PragmaExceptionDialogSupport
{
  private PragmaExceptionDialogSupport() { }

  public static void showError(String title, Exception e)
  {
    ExceptionDialog.showAndWait(title, title, buildContent(title, e), e);
  }

  public static String buildContent(String title, Exception e)
  {
    String message = e.getMessage();
    StringBuilder result = new StringBuilder();
    if (message != null && !message.isBlank()) result.append(message.trim());

    List<String> hints = hints(e, title);
    if (!hints.isEmpty())
    {
      if (result.length() > 0) result.append("\n\n");
      result.append("Mögliche Ursachen:\n- ").append(String.join("\n- ", hints));
    }
    return result.toString();
  }

  private static List<String> hints(Exception e, String title)
  {
    Throwable root = rootCause(e);
    String type = root.getClass().getName().toLowerCase(Locale.ROOT);
    String message = safeMessage(root).toLowerCase(Locale.ROOT);
    String titleLower = title == null ? "" : title.toLowerCase(Locale.ROOT);
    List<String> hints = new ArrayList<>();

    if (type.contains("processingexception"))
    {
      hints.add("Der Server ist nicht erreichbar oder hat die Anfrage abgelehnt.");
      hints.add("Pruefe URL, Port, laufende Dienste und Netzwerkverbindung.");
    }

    if (message.contains("communication error"))
    {
      hints.add("Der Client konnte die Antwort nicht verarbeiten oder keinen gueltigen Response erhalten.");
    }

    if (message.contains("http 400") || message.contains("invalid_grant") || message.contains("account is not fully set up"))
    {
      hints.add("Keycloak lehnt den Login ab: Benutzername oder Passwort falsch, Required Actions offen oder Direct Access Grants fehlen.");
      hints.add("Pruefe auch Realm, Client-ID und die Provisionierung des Users.");
    }
    else if (message.contains("http 401") || message.contains("unauthorized"))
    {
      hints.add("Der Aufruf ist nicht autorisiert. Token, Client oder Benutzerrechte sind wahrscheinlich falsch.");
    }
    else if (message.contains("http 403") || message.contains("forbidden"))
    {
      hints.add("Der Benutzer ist angemeldet, hat aber fuer diese Aktion nicht genug Rechte.");
    }
    else if (message.contains("http 404") || message.contains("not found"))
    {
      hints.add("Der Endpunkt oder die Ressource existiert nicht. Moeglich sind falsche URL, Port oder Backend-Version.");
    }

    if (message.contains("connection refused") || message.contains("connect timed out") || message.contains("timeout"))
    {
      hints.add("Der Zielserver laeuft wahrscheinlich nicht oder reagiert zu langsam.");
    }

    if (message.contains("parse") || message.contains("deserial") || type.contains("json"))
    {
      hints.add("Die Serverantwort hat ein unerwartetes Format oder enthaelt unerwartete Daten.");
    }

    if (titleLower.contains("save") || titleLower.contains("create") || titleLower.contains("edit") || titleLower.contains("delete"))
    {
      hints.add("Die Aktion ist schreibend. Pruefe, ob der Benutzer die erforderlichen Rollen fuer diese Aenderung hat.");
    }

    if (hints.isEmpty())
    {
      hints.add("Bitte Serverstatus, Konfiguration und die Stacktrace-Ansicht unten pruefen.");
    }

    return hints;
  }

  private static Throwable rootCause(Throwable throwable)
  {
    Throwable root = throwable;
    while (root.getCause() != null && root.getCause() != root) root = root.getCause();
    return root;
  }

  private static String safeMessage(Throwable throwable)
  {
    String message = throwable.getMessage();
    return message == null ? "" : message;
  }
}
