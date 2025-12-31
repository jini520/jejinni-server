package site.jejinni.server.controller.career;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.jejinni.server.dto.career.*;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.service.career.CareerService;

import java.util.UUID;

@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {

  private final CareerService careerService;

  // 전체 조회
  @GetMapping
  public ResponseEntity<ApiResponse<CareersDto>> getAllCareers() {
    ApiResponse<CareersDto> careers = careerService.getAllCareers();
    return ResponseEntity.ok(careers);
  }

  // ===== Business 엔드포인트 =====
  @PostMapping("/business")
  public ResponseEntity<ApiResponse<BusinessDto>> createBusiness(@RequestBody BusinessRequestDto dto) {
    ApiResponse<BusinessDto> created = careerService.createBusiness(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/business/{id}")
  public ResponseEntity<ApiResponse<BusinessDto>> getBusinessById(@PathVariable UUID id) {
    ApiResponse<BusinessDto> business = careerService.getBusinessById(id);
    return ResponseEntity.ok(business);
  }

  @PutMapping("/business/{id}")
  public ResponseEntity<ApiResponse<BusinessDto>> updateBusiness(
      @PathVariable UUID id,
      @RequestBody BusinessRequestDto dto) {
    ApiResponse<BusinessDto> updated = careerService.updateBusiness(id, dto);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/business/{id}")
  public ResponseEntity<Void> deleteBusiness(@PathVariable UUID id) {
    careerService.deleteBusiness(id);
    return ResponseEntity.noContent().build();
  }

  // ===== Career Project 엔드포인트 =====
  @PostMapping("/projects")
  public ResponseEntity<ApiResponse<CareerProjectDto>> createCareerProject(@RequestBody CareerProjectRequestDto dto) {
    ApiResponse<CareerProjectDto> created = careerService.createCareerProject(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/projects/{id}")
  public ResponseEntity<ApiResponse<CareerProjectDto>> getCareerProjectById(@PathVariable UUID id) {
    ApiResponse<CareerProjectDto> project = careerService.getCareerProjectById(id);
    return ResponseEntity.ok(project);
  }

  @PutMapping("/projects/{id}")
  public ResponseEntity<ApiResponse<CareerProjectDto>> updateCareerProject(
      @PathVariable UUID id,
      @RequestBody CareerProjectRequestDto dto) {
    ApiResponse<CareerProjectDto> updated = careerService.updateCareerProject(id, dto);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/projects/{id}")
  public ResponseEntity<Void> deleteCareerProject(@PathVariable UUID id) {
    careerService.deleteCareerProject(id);
    return ResponseEntity.noContent().build();
  }
}
