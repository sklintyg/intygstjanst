Genomgång av scenarier och berörda fält i FK7263-blankett:

Scenario#:   Fält:

1	     1, 8b, 14-17

2	     1, 8b, 10, 14-17

3	     1, 8b, 10, 13, 14-17

4	     2b, 4a, 4b, 5, 8a, 8b, 14-17

5	     2a, 2b, 4a, 4b, 5, 8a, 8b, 14-17

6	     2a, 2b, 3, 4a, 4b, 5, 8a, 8b, 14-17

7	     2b, 4a, 4b, 5, 6b, 8a, 8b, 11, 13, 14-17

8	     2b, 4a, 4b, 5, 8a, 8b, 11, 14-17

9	     2a, 2b, 4a, 4b, 5, 7, 8a, 8b, 9, 10, 11, 14-17

10	     2a, 2b, 3, 4a, 4b, 5, 6b, 7, 8a, 8b, 9, 10, 12, 14-17

11	     2a, 2b, 3, 4a, 4b, 5, 6a, 6b, 7, 8a, 8b, 9, 10, 14-17

12	     2a, 2b, 3, 4a, 4b, 5, 6a, 6b, 7, 8a, 8b, 9, 10, 12, 13, 14-17

13	     2a, 2b, 3, 4a, 4b, 5, 6a, 6b, 7, 8a, 8b, 9, 10, 11, 12, 13, 14-17


///////////////////////////////////////////////////////////////////////////////////////////////////////


FK7263 Mappning av formulärfält mot XML

Fält#: 		Beskrivning:			XML:
-----------------------------------------------------------------------------
1		Smittskydds-checkbox
						<aktivitet>
						  <aktivitetskod code="AKT11"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
						  	codeSystemName="kv_aktiviteter_intyg"/>
						</aktivitet>
-----------------------------------------------------------------------------
2a & 2b		Diagnos för sjukdom		
						 <observation>
						   <observationskategori code="439401001"
						   	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
    						   <observationskod code="S47" codeSystemName="ICD-10"/>
    						   <beskrivning>Diagnos för sjukdom - beskrivning</beskrivning>
  						 </observation>

2b		Diagnoskod
						 <observation>
						   <observationskategori code="439401001"
						   	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
    						   <observationskod code="S47" codeSystemName="ICD-10"/>
  						 </observation>

-----------------------------------------------------------------------------
3 		Sjukdomsförlopp
						<observation>
						  <observationskod code="288524001"
						  	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
						  <beskrivning>Sjukdomsförlopp
						  </beskrivning>
						</observation>
-----------------------------------------------------------------------------						
4a		Funktionsnedsättning		
		observationer
						<observation>
    						  <observationskategori code="b"
							codeSystem="1.2.752.116.1.1.3.1.1" codeSystemName="ICF"/>
    						  <beskrivning>Funktionsnedsättning - observationer
      						  </beskrivning>
						</observation>
-----------------------------------------------------------------------------						
4b		Intyg baseras på:		
		*Min undersökning
						<vardkontakt>
						  <vardkontaktTyp code="5880005" codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
						    <vardkontaktTid>
					 	    <from></from>
						    <tom></tom>
						  </vardkontaktTid>
						</vardkontakt>

		*Min telefonkontakt
						<vardkontakt>
						  <vardkontaktTyp code="185317003"
						  	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
						    <vardkontaktTid>
					 	    <from></from>
						    <tom></tom>
						  </vardkontaktTid>
						</vardkontakt>

-----------------------------------------------------------------------------
4b		Intyg baseras på:		
		* Journaluppgift
						<referens>
						  <referenstyp code="419891008" codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
						  <referensdatum>
		  		     	  	  </referensdatum>
						</referens>

		* Annat (kräver även fält 13)
						<referens>
						  <referenstyp code="74964007" codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
						  <referensdatum>
		  		     	  	  </referensdatum>
						</referens>

-----------------------------------------------------------------------------
5		Aktivitetsbegränsning
						<observation>
						  <observationskategori code="d"
						  	codeSystem="1.2.752.116.1.1.3.1.1" codeSystemName="ICF"/>
    						  <beskrivning> Aktivitetsbegränsning
    						  </beskrivning>
						</observation>
-----------------------------------------------------------------------------
6a		Rekommendationer		
		* Kontakt arbetsförmedling	  
						<aktivitet>
						  <aktivitetskod code="AKT6"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
							codeSystemName="kv_aktiviteter_intyg"/>
  						</aktivitet>

		* Kontakt företagshälsovård
						<aktivitet>	
						  <aktivitetskod code="AKT7"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
							codeSystemName="kv_aktiviteter_intyg"/>
						</aktivitet>

		* övrigt
						<aktivitet>
						  <aktivitetskod code="AKT12"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
							codeSystemName="kv_aktiviteter_intyg"/>
						  <beskrivning>Övrig rekommendation
						  </beskrivning>
						</aktivitet>
-----------------------------------------------------------------------------
6b		Planerad eller pågående		
		behandling / åtgärd
		*Inom sjukvården:
						<aktivitet>
						  <aktivitetskod code="AKT1"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
							codeSystemName="kv_aktiviteter_intyg"/>
						  <beskrivning>Planerad aktivitet inom sjukvården
						  </beskrivning>
						</aktivitet>

		*Annan åtgärd:
						<aktivitet>
						  <aktivitetskod code="AKT2"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
							codeSystemName="kv_aktiviteter_intyg"/>
						  <beskrivning>Annan åtgärd - ange vilken
						  </beskrivning>
						</aktivitet>
					
-----------------------------------------------------------------------------
7		Är arbetslivsinriktad		
		rehab aktuellt?
		
						<aktivitet>
						  <aktivitetskod code="AKT3-AKT5"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
							codeSystemName="kv_aktiviteter_intyg"/>
						</aktivitet>
						
-----------------------------------------------------------------------------
8a		Arbetsförmåga bedöms i		
		förhållande till (placeras i Patient)
		*nuvarande arbete:
						<arbetsuppgift>
						  <typAvArbetsuppgift>
						  Beskrivning
						  </typAvArbetsuppgift>
						</arbetsuppgift>
						<sysselsattning>
						  <typAvSysselsattning code="224375002"
						  	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT" />
						</sysselsattning>
		*arbetslöshet:
						<arbetsuppgift>
						  <typAvArbetsuppgift>
						  Arbetslöshet
						  </typAvArbetsuppgift>
						</arbetsuppgift>
						<sysselsattning>
						  <typAvSysselsattning code="73438004"
						  	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT" />
						</sysselsattning>

		*mammeledighet:
						<arbetsuppgift>
						  <typAvArbetsuppgift>
						  Mammaledighet
						  </typAvArbetsuppgift>
						</arbetsuppgift>
						<sysselsattning>
						  <typAvSysselsattning code="224457004"
							codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT" />
						</sysselsattning>

		*pappaledighet:
						<arbetsuppgift>
						  <typAvArbetsuppgift>
						  Pappaledighet
						  </typAvArbetsuppgift>
						</arbetsuppgift>
						<sysselsattning>
						  <typAvSysselsattning code="224458009"
						  	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT" />
						</sysselsattning>
					
-----------------------------------------------------------------------------
8b		Jag bedömer att patientens	
		arbetsförmåga är		  
						<observation>
						    <observationskod code="302119000"
						    	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
						    <observationsperiod>
						    <from></from>
						    <tom></tom>
						  </observationsperiod>
						  <varde value="[25.0 - 100.0]" unit="percent"/>
						</observation>
-----------------------------------------------------------------------------
9		Patientens förmåga bedöms	
		nedsatt längre än vad som
		kan anges ovan
						<observation>
						    <observationskod code="302119000"
						    	codeSystem="1.2.752.116.2.1.1.1" codeSystemName="SNOMED-CT"/>
						    <observationsperiod>
						    <from></from>
						    <tom></tom>
						  </observationsperiod>
						  <varde value="[25.0 - 100.0]" unit="percent"/>
						  <prognos>
						     <prognoskod code="PRO1 - PRO4"
						     	codeSystem="3de65a8b-ae2c-48ce-b6fe-35bdd1f60cf7"
							codeSystemName="kv_prognos_intyg" />
						     <beskrivning>
						     Patientens förmåga bedöms nedsatt längre än vad som anges ovan pga:
						     </beskrivning>
						  </prognos>
						</observation>
-----------------------------------------------------------------------------
10 		Prognos - kommer patienten
		få tillbaka arbetsfömåga
						<observation>
						  <prognos>
						    <prognoskod code="PRO1 - PRO4"
						    	codeSystem="3de65a8b-ae2c-48ce-b6fe-35bdd1f60cf7"
							codeSystemName="kv_prognos_intyg" />
						  </prognos>
						</observation>
-----------------------------------------------------------------------------
11		Kan alternativt färdsätt	
		möjliggöra tidigare		  
		återgång till arbete?
						<aktivitet>
						  <aktivitetskod code="AKT8-AKT9"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
							codeSystemName="kv_aktiviteter_intyg"/>
						</aktivitet>
-----------------------------------------------------------------------------
12		Kontakt med FK önskas
						<aktivitet>
						  <aktivitetskod code="AKT10"
						  	codeSystem="8040b4d1-67dc-42e1-a938-de5374e9526a"
							codeSystemName="kv_aktiviteter_intyg"/>
						</aktivitet>
-----------------------------------------------------------------------------
13		Övriga upplysningar
						<kommentar>
						</kommentar>
-----------------------------------------------------------------------------
14		Datum				Någon form av datum, men
						vilken typ av vårdkontakt?!
						
-----------------------------------------------------------------------------
15		Namn, mottagningsadress		Sätts kanske inte?

-----------------------------------------------------------------------------
17		Arbetsplatskod
						<skapadAv>
						  <personal-id root="1.2.752.129.2.1.4.1" extension="Personal HSA-ID"/>
						  <fullstandigtNamn> </fullstandigtNamn>
						  <enhet>
						    <hr:enhets-id root="1.2.752.129.2.1.4.1" extension=""/>
			    			    <hr:arbetsplatskod root="1.2.752.29.4.71" extension=""/>
      						    <hr:enhetsnamn></hr:enhetsnamn>
      						    <hr:postadress></hr:postadress>
      						    <hr:postnummer></hr:postnummer>
      						    <hr:postort></hr:postort>
      						    <hr:telefonnummer></hr:telefonnummer>
      						    <hr:epost></hr:epost>
						    <hr:vardgivare>
		 				      <hr:vardgivare-id root="1.2.752.129.2.1.4.1"
						      		extension="VardgivarId"/>
						      <hr:vardgivarnamn></hr:vardgivarnamn>
						    </hr:vardgivare>
						  </enhet>
						</skapadAv>
