# Introduktion
Her kan du finde information og kodeeksempler på anvendelse af XDS infrastruktur i forhold til deling af aftaler.
Eksemplerne er skrevet i Java.

## CDA dokumenter og aftaler
Data i en XDS infrastruktur gemmes som dokumenter. Et dokument har et unikt id (documentId) og en række metadata, der beskriver, hvad dokumentet handler om.
Dokumenter kan indholdsmæssigt være af flere forskellige typer: PDF, Word-dokument, men kan også være af typen CDA (Clinical Document Architecture). 
Et CDA dokument er egentlig bare et XML dokument, der følger en bestemt standard for kliniske dokumenter til udveksling (deling). 
Se f.eks. [What is HL7 CDA?] (http://iehr.eu/knowledge/what-is-hl7-cda/) for en kort beskrivelse. Her kan man bl.a. se, at CDA findes på forskellige niveauer 1-3, hvor 3 har den højeste grad af struktur.
En vigtig egenskab ved CDA dokumenter er den fælles CDA header. Denne header indeholder information, der går igen henover alle typer af kliniske dokumenter f.eks. hvilken patient drejer dokumentet sig om, hvilken organisation er ansvarlig (ejer) af dokumentet mm.
CDA headeren er således en international standard (HL7), men der findes en dansk specialisering af denne (standardiseret i regi af Medcom). Denne er beskrevet her: [HL7 Implementation Guide for CDA Release 2.0 CDA Header (DK CDA Header) Draft for Trial Use Release 1.1](http://svn.medcom.dk/svn/drafts/Standarder/HL7/CDA%20Header/Dokumentation/DK-CDA-Header-v1.1.pdf)

Der findes andre profiler, der er specialiseringer af CDA. Dvs. kliniske dokumenter, der har en struktur til bestemte formål. 
Medcom har leveret danske profileringer af følgende typer (se evt. [Medcoms oversigt over HL7 standarder](http://svn.medcom.dk/svn/drafts/Standarder/HL7/)):
* Personal Health Monitoring Report (PHMR) til hjemmemålinger
* Questionnaire Form Definition Document (QFDD) og Questionnaire Response Document (QRD) til patientrapporterede oplysninger (PRO)
* Appointment Document (APD) til aftaler


## Services
XDS Repository og Registry kan anvendes til at gemme, fremsøge og hente (CDA) dokumenter.
XDS Repository står for opbevaringen af dokumenterne tilknyttet et unikt ID. I dette tilfælde er det et 

XDS Registry står for opbevaring og indexering af metadata vedr. dokumenterne. Dette kunne f.eks. være start- og sluttidspunkt for af


