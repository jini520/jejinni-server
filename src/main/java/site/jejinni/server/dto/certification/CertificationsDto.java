package site.jejinni.server.dto.certification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationsDto {
	private List<CertificationDto> certifications;
	private List<AwardDto> awards;
}

