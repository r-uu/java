package de.ruu.lib.util.mail.thunderbird;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;

public class FolderGenerator {

	public static void main(String[] args) {
		// Ihr spezifischer Thunderbird-Pfad
		String basePathStr = "/mnt/c/Users/r-uu/AppData/Roaming/Thunderbird/Profiles/qlhopqta.default-release/Mail/Local Folders";
		// Ihre Ordnerstruktur als strukturierter String
		String structure = """
            - 01-invoice
              - 01-health
                - 01-physicians
                  - 01-nusser
                - 02-pharmacy
                - 03-podiatrist
                  - 01-mike
            - 02-purchase
              - 01-payback
              - 02-paypal
              - 03-online shopping
                - 01-hood
            - 03-contract
              - 01-insurance
              - 02-tenancy agreement
              - 03-car leasing
              - 04-telko
              - 05-versorgung
                - 01-stadtwerke
            - 04-bank
              - 01-c24
              - 02-sparkasse
              - 03-targobank
                - 01-income frauke
              - 04-advanzia
            - 05-tax
              - 01-kfz
            - 06-work
              - 01-opitz consulting
              - 02-mtag
              - 03-gkd
              - 09-application
            - 07-develop
              - 01-github
              - 02-hetzner
              - 03-spaceship
            - 08-private
              - 01-tree pruning
              - 02-fabri website
              - 03-social media
                - 01-linkedin
                - 02-kununu
              - 04-vfl
              - 05-holiday
                - 01-2023 fuerteventura
            - 98-suspected trash
            - 99-uncategorised
            """;

		try {
			Path basePath = Paths.get(basePathStr);
			if (!Files.exists(basePath)) {
				System.err.println("Fehler: Der angegebene Pfad existiert nicht!");
				return;
			}

			createThunderbirdFolders(basePath, structure.split("\n"));
			System.out.println("Struktur erfolgreich in Thunderbird generiert!");

		} catch (IOException e) {
			System.err.println("Ein Fehler ist beim Erstellen der Ordner aufgetreten: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void createThunderbirdFolders(Path basePath, String[] lines) throws IOException {
		Stack<Path> pathStack = new Stack<>();
		Stack<Integer> indentStack = new Stack<>();

		pathStack.push(basePath);
		indentStack.push(-1);

		for (String line : lines) {
			if (line.trim().isEmpty()) {
				continue;
			}

			// Bestimme die Einrückungsebene anhand der führenden Leerzeichen
			int indent = line.length() - line.stripLeading().length();

			// Ordnernamen isolieren und bereinigen
			String name = line.trim().replaceFirst("^-\\s*", "").trim();

			// Stack anpassen, wenn wir uns in der Hierarchie wieder nach oben bewegen
			while (indent <= indentStack.peek()) {
				pathStack.pop();
				indentStack.pop();
			}

			Path currentParent = pathStack.peek();

			// 1. Die Mbox-Datei anlegen (leere Datei ohne Endung für die Mails selbst)
			Path folderFile = currentParent.resolve(name);
			if (!Files.exists(folderFile)) {
				Files.createFile(folderFile);
			}

			// 2. Den .sbd-Ordner anlegen (wird zwingend für Unterordner benötigt)
			Path sbdDir = currentParent.resolve(name + ".sbd");
			if (!Files.exists(sbdDir)) {
				Files.createDirectories(sbdDir);
			}

			// Für die nächste Iteration auf den Stack legen
			pathStack.push(sbdDir);
			indentStack.push(indent);
		}
	}
}