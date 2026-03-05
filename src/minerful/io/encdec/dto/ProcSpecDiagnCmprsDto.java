package minerful.io.encdec.dto;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import minerful.checking.diagnosis.ConstraintDiagnosis;
import minerful.checking.diagnosis.DiagnosisRecord;
import minerful.checking.diagnosis.LogCoordinates;
import minerful.checking.diagnosis.ProcessSpecificationDiagnosis;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.relation.Response;
import minerful.io.encdec.dto.ProcSpecDiagnCmprsDto.CnsDiagnEntrCmprsDto.TrcCllectrCmprsDto;
import minerful.io.encdec.dto.ProcSpecDiagnCmprsDto.CnsDiagnEntrCmprsDto.TrcCllectrCmprsDto.EvtPairCmprsDto;

public class ProcSpecDiagnCmprsDto {
	public static class CnsDiagnEntrCmprsDto implements Comparable<CnsDiagnEntrCmprsDto> {
		public static class TrcCllectrCmprsDto implements Comparable<TrcCllectrCmprsDto> {			
			public static class EvtPairCmprsDto implements Comparable<EvtPairCmprsDto> {
				public final int triggerEvtNum;
				public final int checkEvtNum;
				public EvtPairCmprsDto(int triggerEvtNum, int checkEvtNum) {
					this.triggerEvtNum = triggerEvtNum;
					this.checkEvtNum = checkEvtNum;
				}
				@Override public int compareTo(EvtPairCmprsDto other) { return (this.triggerEvtNum < other.triggerEvtNum ? -1 : (this.triggerEvtNum > other.triggerEvtNum ? 1 : 0)); }
				@Override public boolean equals(Object other) { try { return this.compareTo((EvtPairCmprsDto) other) == 0; } catch (ClassCastException e) { return false; } }
				
				public String toJsonString() {
					return String.format("[%s,%s]",
							LogCoordinates.eventNumberToString(triggerEvtNum),
							LogCoordinates.eventNumberToString(checkEvtNum));
				}
			}

			public final Set<EvtPairCmprsDto> pairSet;
			public final int traceNum;

			public TrcCllectrCmprsDto(int traceNum) {
				this.pairSet = new TreeSet<EvtPairCmprsDto>();
				this.traceNum = traceNum;	
			}
			@Override public int compareTo(TrcCllectrCmprsDto other) { return (this.traceNum < other.traceNum ? -1 : (this.traceNum > other.traceNum ? 1 : 0)); }
			@Override public boolean equals(Object other) { try { return this.compareTo((TrcCllectrCmprsDto) other) == 0; } catch (ClassCastException e) { return false; } }

			public String toJsonString() {
				return String.format("\"%s\":[%s]",
						traceNum,
						pairSet.stream().map(x -> x.toJsonString()).collect(Collectors.joining(",")));
			}
		}
		
		public final TreeSet<TrcCllectrCmprsDto> satisfactions, violations;
		
		public static String VIOLATION_KEY = "VIOL", SATISFACTION_KEY = "SAT";
		public final Constraint cns;

		public CnsDiagnEntrCmprsDto(Constraint cns) {
			this.cns = cns;
			satisfactions = new TreeSet<TrcCllectrCmprsDto>();
			violations =  new TreeSet<TrcCllectrCmprsDto>();
		}
		public CnsDiagnEntrCmprsDto(ConstraintDiagnosis cnsDiagno) {
			this(cnsDiagno.cns);
			for (DiagnosisRecord rec: cnsDiagno.getDiagnosisRecords()) {
				TreeSet<TrcCllectrCmprsDto> setToEnrich = null;
				switch (rec.status) {
				case SATISFIES:
					setToEnrich = satisfactions;
					break;
				case VIOLATES:
					setToEnrich = violations;
					break;
				default:
					break;
				}
				if (setToEnrich != null) {
					if (!rec.trigger.coordinates.caseNumber.equals(rec.validator.coordinates.caseNumber)) {
						throw new IllegalArgumentException(
								String.format("This data structure cannot deal with triggers and validators on different cases "
										+ "(%d and %d, respectively)",
										rec.trigger.coordinates.caseNumber,
										rec.validator.coordinates.caseNumber)
								);
					}
					TrcCllectrCmprsDto trcDto = new TrcCllectrCmprsDto(rec.trigger.coordinates.caseNumber);
					if (setToEnrich.contains(trcDto)) {
						trcDto = setToEnrich.floor(trcDto);
					} else {
						setToEnrich.add(trcDto);
					}
					trcDto.pairSet.add(new EvtPairCmprsDto(rec.trigger.coordinates.eventNumber, rec.validator.coordinates.eventNumber));
				}
			}
		}
		@Override public int compareTo(CnsDiagnEntrCmprsDto other) { return this.cns.compareTo(other.cns); }
		@Override public boolean equals(Object other) { try { return this.compareTo((CnsDiagnEntrCmprsDto) other) == 0; } catch (ClassCastException e) { return false; } }	
		
		public String toJsonString() {
			return String.format("\"%s\":{\"%s\":{%s},\"%s\":{%s}}",
					cns.toString(),
					SATISFACTION_KEY, satisfactions.stream().map(x -> x.toJsonString()).collect(Collectors.joining(",")),
					VIOLATION_KEY, violations.stream().map(x -> x.toJsonString()).collect(Collectors.joining(","))
			);
		}
	}

	public final Set<CnsDiagnEntrCmprsDto> diagnoses;
	
	public ProcSpecDiagnCmprsDto() {
		this.diagnoses = new TreeSet<CnsDiagnEntrCmprsDto>();
	}
	
	public ProcSpecDiagnCmprsDto(ProcessSpecificationDiagnosis processSpecificationDiagnosis) {
		this();
		
		for (ConstraintDiagnosis cnsDiagno : processSpecificationDiagnosis.cnsDiagnoses) {
			diagnoses.add(new CnsDiagnEntrCmprsDto(cnsDiagno));
		}
	}

	public String toJsonString() {
		return String.format("{%s}",
				diagnoses.stream().map(x -> x.toJsonString()).collect(Collectors.joining(",\n "))
		);
	}
	
	public static void main(String[] args) {
		ProcSpecDiagnCmprsDto dto = new ProcSpecDiagnCmprsDto();
		CnsDiagnEntrCmprsDto diagnosis = new CnsDiagnEntrCmprsDto(new Response(new TaskChar('A'), new TaskChar('B')));
		TrcCllectrCmprsDto sats = new TrcCllectrCmprsDto(0);
		sats.pairSet.add(new EvtPairCmprsDto(0, 1));
		sats.pairSet.add(new EvtPairCmprsDto(2, LogCoordinates.END_OF_TRACE_EVENT_NUMBER));
		TrcCllectrCmprsDto viols = new TrcCllectrCmprsDto(1);
		viols.pairSet.add(new EvtPairCmprsDto(LogCoordinates.START_OF_TRACE_EVENT_NUMBER, 1));
		viols.pairSet.add(new EvtPairCmprsDto(2, LogCoordinates.END_OF_TRACE_EVENT_NUMBER));
		diagnosis.satisfactions.add(sats);
		diagnosis.violations.add(viols);
		dto.diagnoses.add(diagnosis);
		
		System.out.println(dto.toJsonString());
	}
	
}