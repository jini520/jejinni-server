package site.jejinni.server.service.skill;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.jejinni.server.domain.entity.skill.Category;
import site.jejinni.server.domain.entity.skill.Skill;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.dto.skill.CategoryDto;
import site.jejinni.server.dto.skill.CategoryRequestDto;
import site.jejinni.server.dto.skill.SkillDto;
import site.jejinni.server.dto.skill.SkillRequestDto;
import site.jejinni.server.dto.skill.SkillsDto;
import site.jejinni.server.repository.skill.CategoryRepository;
import site.jejinni.server.repository.skill.SkillRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillService {

	private final CategoryRepository categoryRepository;
	private final SkillRepository skillRepository;

	public ApiResponse<SkillsDto> getAllSkills() {
		List<Category> categories = categoryRepository.findAllByOrderByOrderAsc();
		List<Skill> skills = skillRepository.findAllByOrderByOrderAsc();

		SkillsDto data = SkillsDto.builder()
				.categories(categories.stream()
						.map(this::toCategoryDto)
						.collect(Collectors.toList()))
				.skills(skills.stream()
						.map(this::toSkillDto)
						.collect(Collectors.toList()))
				.build();

		return new ApiResponse<>(data);
	}

	public ApiResponse<List<CategoryDto>> getCategories() {
		List<Category> categories = categoryRepository.findAllByOrderByOrderAsc();
		List<CategoryDto> data = categories.stream()
				.map(this::toCategoryDto)
				.collect(Collectors.toList());

		return new ApiResponse<>(data);
	}

	public ApiResponse<CategoryDto> getCategory(UUID id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

		return new ApiResponse<>(toCategoryDto(category));
	}

	@Transactional
	public ApiResponse<CategoryDto> createCategory(CategoryRequestDto request) {
		Category category = Category.builder()
				.name(request.getName())
				.order(request.getOrder())
				.build();

		category = categoryRepository.save(category);

		return new ApiResponse<>(toCategoryDto(category));
	}

	@Transactional
	public ApiResponse<CategoryDto> updateCategory(UUID id, CategoryRequestDto request) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

		if (request.getName() != null) {
			category.updateName(request.getName());
		}
		if (request.getOrder() != null) {
			category.updateOrder(request.getOrder());
		}

		return new ApiResponse<>(toCategoryDto(category));
	}

	@Transactional
	public void deleteCategory(UUID id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

		// 카테고리에 속한 스킬이 있는지 확인
		if (skillRepository.existsByCategory(category)) {
			throw new IllegalStateException("Cannot delete category: Skills exist in this category");
		}

		categoryRepository.delete(category);
	}

	public ApiResponse<SkillDto> getSkill(UUID id) {
		Skill skill = skillRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Skill not found with id: " + id));

		return new ApiResponse<>(toSkillDto(skill));
	}

	@Transactional
	public ApiResponse<SkillDto> createSkill(SkillRequestDto request) {
		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

		Skill skill = Skill.builder()
				.name(request.getName())
				.category(category)
				.order(request.getOrder())
				.build();

		skill = skillRepository.save(skill);

		return new ApiResponse<>(toSkillDto(skill));
	}

	@Transactional
	public ApiResponse<SkillDto> updateSkill(UUID id, SkillRequestDto request) {
		Skill skill = skillRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Skill not found with id: " + id));

		if (request.getName() != null) {
			skill.updateName(request.getName());
		}
		if (request.getCategoryId() != null) {
			Category category = categoryRepository.findById(request.getCategoryId())
					.orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));
			skill.updateCategory(category);
		}
		if (request.getOrder() != null) {
			skill.updateOrder(request.getOrder());
		}

		return new ApiResponse<>(toSkillDto(skill));
	}

	@Transactional
	public void deleteSkill(UUID id) {
		Skill skill = skillRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Skill not found with id: " + id));

		skillRepository.delete(skill);
	}

	private CategoryDto toCategoryDto(Category category) {
		return CategoryDto.builder()
				.id(category.getId())
				.name(category.getName())
				.order(category.getOrder())
				.build();
	}

	private SkillDto toSkillDto(Skill skill) {
		return SkillDto.builder()
				.id(skill.getId())
				.name(skill.getName())
				.categoryId(skill.getCategory().getId())
				.order(skill.getOrder())
				.build();
	}
}

