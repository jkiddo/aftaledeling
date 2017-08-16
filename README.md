# Introduktion
Her kan du finde information og kodeeksempler på anvendelse af XDS infrastruktur i forhold til deling af aftaler.
Eksemplerne er skrevet i Java.

Først gennemgåes de centrale koncepter i forhold til aftaledeling

## Hvordan beskrives aftaler: CDA dokumenter og aftaledokumenter
Data i en XDS infrastruktur gemmes som dokumenter. Et dokument har et unikt id (documentId) og en række metadata, der beskriver, hvad dokumentet handler om.
Dokumenter kan indholdsmæssigt være af flere forskellige typer: PDF, Word-dokument, men kan også være af typen CDA (Clinical Document Architecture). 
Et CDA dokument er egentlig bare et struktureret XML dokument, der følger en bestemt standard for kliniske dokumenter til udveksling (deling). 
Se f.eks. [What is HL7 CDA?] (http://iehr.eu/knowledge/what-is-hl7-cda/) for en kort beskrivelse. Her kan man bl.a. se, at CDA findes på forskellige niveauer 1-3, hvor 3 har den højeste grad af struktur.
En vigtig egenskab ved CDA dokumenter er den fælles CDA header. Denne header indeholder information, der går igen henover alle typer af kliniske dokumenter f.eks. hvilken patient drejer dokumentet sig om, hvilken organisation er ansvarlig (ejer) af dokumentet mm.
CDA headeren er således en international standard (HL7), men der findes en dansk specialisering af denne (standardiseret i regi af Medcom). Denne er beskrevet her: [HL7 Implementation Guide for CDA Release 2.0 CDA Header (DK CDA Header) Draft for Trial Use Release 1.1](http://svn.medcom.dk/svn/drafts/Standarder/HL7/CDA%20Header/Dokumentation/DK-CDA-Header-v1.1.pdf)

Der findes andre profiler, der er specialiseringer af CDA. Dvs. kliniske dokumenter, der har en struktur til bestemte formål. 
Medcom har leveret danske profileringer af følgende typer (se evt. [Medcoms oversigt over HL7 standarder](http://svn.medcom.dk/svn/drafts/Standarder/HL7/)):
* Personal Health Monitoring Report (PHMR) til hjemmemålinger
* Questionnaire Form Definition Document (QFDD) og Questionnaire Response Document (QRD) til patientrapporterede oplysninger (PRO)
* Appointment Document (APD) til aftaler

I forhold til aftaledeling er det dokumenter af type APD der er relevant.
Når en aftale ønskes delt skal den derfor beskrives i et CDA dokument, der følger standarden beskrevet i (HL7 Implementation Guide for CDA Release 2.0 Appointment Document (Danish profile – DK APD) Draft for Trial Use Release 1.0)[http://svn.medcom.dk/svn/drafts/Standarder/HL7/Appointment/Dokumentation/DK-APD-v1.0.docx] med en header, der lever op til den danske profilering af CDA header.

## Hvordan deles data: XDS overblik og services
En XDS infrastruktur består (mindst) af følgende to komponenter:
* XDS Repository: Står for persistering af dokumenter tilknyttet et unikt ID. 
* XDS Registry: Står for opbevaring og indexering af metadata vedr. dokumenterne i et eller flere XDS repositories. Dette kunne f.eks. være start- og sluttidspunkt for en aftale, patienten, som aftalen vedrører mm (oplysningerne stammer fra CDA headeren)
Integrationen med XDS infrastrukturen sker vha en række standardiserede SOAP webservices. Et overblik over XDS infrastrukturen og de forskellige services ses nedenfor:
![Billede af XDS Infrastruktur og ITI services burde være her](http://wiki.ihe.net/images/d/d7/XDS-Actor-Transaction-b.jpg "XDS komponenter og ITI services")

Når data skal deles vha XDS sker følgende:
1. Dokumenter afleveres af dokumentkilden (Document Source) til XDS repository via servicehåndtaget *ITI-41 Provide and Register Document Set*
2. Dokumentaftager (Document Consumer) fremsøger dokumenter i XDS registry via servicehåndtaget *ITI-18 Registry Stored Query*. Svaret på denne query er en liste af documentIds og repositoryIds, der fortæller, hvilke dokumenter der lever op til søgekriterierne, og hvor de findes (repositoryId)
3. Dokumentaftager (Document Consumer) henter dokument i XDS repositroy via servicehåndtaget *ITI-43 Retrieve Document Set*
