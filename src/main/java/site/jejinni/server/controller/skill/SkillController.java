package site.jejinni.server.controller.skill;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.dto.skill.CategoryDto;
import site.jejinni.server.dto.skill.CategoryRequestDto;
import site.jejinni.server.dto.skill.SkillDto;
import site.jejinni.server.dto.skill.SkillRequestDto;
import site.jejinni.server.dto.skill.SkillsDto;
import site.jejinni.server.service.skill.SkillService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

	private final SkillService skillService;

	@GetMapping
	public ResponseEntity<ApiResponse<SkillsDto>> getAllSkills() {
		ApiResponse<SkillsDto> skills = skillService.getAllSkills();
		return ResponseEntity.ok(skills);
	}

	@GetMapping("/categories")
	public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
		ApiResponse<List<CategoryDto>> categories = skillService.getCategories();
		return ResponseEntity.ok(categories);
	}

	@GetMapping("/categories/{id}")
	public ResponseEntity<ApiResponse<CategoryDto>> getCategory(@PathVariable UUID id) {
		ApiResponse<CategoryDto> category = skillService.getCategory(id);
		return ResponseEntity.ok(category);
	}

	@PostMapping("/categories")
	public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@RequestBody CategoryRequestDto request) {
		ApiResponse<CategoryDto> category = skillService.createCategory(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(category);
	}

	@PutMapping("/categories/{id}")
	public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
			@PathVariable UUID id,
			@RequestBody CategoryRequestDto request) {
		ApiResponse<CategoryDto> category = skillService.updateCategory(id, request);
		return ResponseEntity.ok(category);
	}

	@DeleteMapping("/categories/{id}")
	public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
		skillService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<SkillDto>> getSkill(@PathVariable UUID id) {
		ApiResponse<SkillDto> skill = skillService.getSkill(id);
		return ResponseEntity.ok(skill);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<SkillDto>> createSkill(@RequestBody SkillRequestDto request) {
		ApiResponse<SkillDto> skill = skillService.createSkill(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(skill);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<SkillDto>> updateSkill(
			@PathVariable UUID id,
			@RequestBody SkillRequestDto request) {
		ApiResponse<SkillDto> skill = skillService.updateSkill(id, request);
		return ResponseEntity.ok(skill);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSkill(@PathVariable UUID id) {
		skillService.deleteSkill(id);
		return ResponseEntity.noContent().build();
	}
}

