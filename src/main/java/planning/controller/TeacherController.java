package planning.controller;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import planning.exception.InvalidRequestException;
import planning.exception.ResourceConflictException;
import planning.exception.ResourceNotFoundException;
import planning.message.PlanMessage;
import planning.message.TeacherMessage;
import planning.model.Plan;
import planning.model.ResFact;
import planning.model.Result;
import planning.model.Teacher;
import planning.modelVO.TeacherAddVO;
import planning.modelVO.TeacherTimeVO;
import planning.modelVO.TeacherVO;
import planning.repository.PlanCRUD;
import planning.repository.TeacherCRUD;
import planning.repository.TeacherTimeCRUD;
import planning.service.TeacherService;

import javax.validation.constraints.NotNull;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/teacher")
@AllArgsConstructor
public class TeacherController {

    private final TeacherCRUD teacherCRUD;
    private final TeacherTimeCRUD teacherTimeCRUD;
    private final PlanCRUD planCRUD;
    private final TeacherService teacherService;

    @PostMapping(value = "")
    public ResponseEntity<Result<TeacherVO>> addTeacher(@RequestParam(value = "teacherVO") @NotNull String teacherVO,
                                                        @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        TeacherAddVO teacherAddVO = new Gson().fromJson(teacherVO, TeacherAddVO.class);

        if (avatar != null && avatar.getContentType() != null && !avatar.getContentType().equalsIgnoreCase("image/jpeg") && !avatar.getContentType().equalsIgnoreCase("image/png") && !avatar.getContentType().equalsIgnoreCase("image/jpg")) {
            throw InvalidRequestException.getInstance(TeacherMessage.getInvalidFormatAvatar());
        }

        if (avatar != null && avatar.getSize() > 1048576)
            throw InvalidRequestException.getInstance(TeacherMessage.getInvalidSizeAvatar());

        if (teacherAddVO.getUsername() != null && teacherCRUD.getTeacherByUsername(teacherAddVO.getUsername()) != null)
            throw ResourceConflictException.getInstance(TeacherMessage.getDuplicateTeacher(teacherAddVO.getUsername()));

        Teacher teacher = teacherService.addTeacher(teacherAddVO, avatar);

        return ResponseEntity.ok(ResFact.<TeacherVO>build()
                .setResult(teacherService.getTeacherVO(teacher))
                .get());
    }

    @PutMapping(value = "/{teacherId}")
    public ResponseEntity<Result<TeacherVO>> updateTeacher(@PathVariable("teacherId") @NotNull Long teacherId,
                                                           @RequestParam(value = "teacherVO") @NotNull String teacherVO,
                                                           @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        TeacherAddVO teacherAddVO = new Gson().fromJson(teacherVO, TeacherAddVO.class);

        if (avatar != null && avatar.getContentType() != null && !avatar.getContentType().equalsIgnoreCase("image/jpeg") && !avatar.getContentType().equalsIgnoreCase("image/png") && !avatar.getContentType().equalsIgnoreCase("image/jpg")) {
            throw InvalidRequestException.getInstance(TeacherMessage.getInvalidFormatAvatar());
        }

        if (avatar != null && avatar.getSize() > 1048576)
            throw InvalidRequestException.getInstance(TeacherMessage.getInvalidSizeAvatar());

        Teacher teacher = teacherCRUD.getTeacherById(teacherId);

        if (teacher == null)
            throw ResourceNotFoundException.getInstance(TeacherMessage.getTeacherNotFound(teacherId.toString()));

        if (teacherAddVO.getUsername() != null && teacherCRUD.checkDuplicateTeacherUsername(teacherId, teacherAddVO.getUsername()) != null)
            throw ResourceConflictException.getInstance(TeacherMessage.getDuplicateTeacher(teacherAddVO.getUsername()));

        Teacher changedTeacher = teacherService.updateTeacher(teacher, teacherAddVO, avatar);

        return ResponseEntity.ok(ResFact.<TeacherVO>build()
                .setResult(teacherService.getTeacherVO(changedTeacher))
                .get());
    }

    @GetMapping(value = "")
    public ResponseEntity<Result<List<TeacherVO>>> getAllTeachers() {
        return ResponseEntity.ok(ResFact.<List<TeacherVO>>build()
                .setResult(teacherService.getTeacherVOs(teacherCRUD.getAllTeachers()))
                .get());
    }

    @GetMapping(value = "/{teacherId}")
    public ResponseEntity<Result<TeacherVO>> getTeacherById(@PathVariable("teacherId") @NotNull Long teacherId) {
        Teacher teacher = teacherCRUD.getTeacherById(teacherId);

        if (teacher == null)
            throw ResourceNotFoundException.getInstance(TeacherMessage.getTeacherNotFound(teacherId.toString()));

        return ResponseEntity.ok(ResFact.<TeacherVO>build()
                .setResult(teacherService.getTeacherVO(teacher))
                .get());
    }

    @DeleteMapping(value = "/{teacherId}")
    public ResponseEntity<Result<Boolean>> deleteTeacherById(@PathVariable("teacherId") @NotNull Long teacherId) {
        Teacher teacher = teacherCRUD.getTeacherById(teacherId);

        if (teacher == null)
            throw ResourceNotFoundException.getInstance(TeacherMessage.getTeacherNotFound(teacherId.toString()));

        teacherService.deleteTeacher(teacher);

        return ResponseEntity.ok(ResFact.<Boolean>build()
                .setResult(true)
                .setMessage(TeacherMessage.getTeacherDeleted(teacherId.toString()))
                .get());
    }

    @PostMapping("/{teacherId}/time")
    public ResponseEntity<Result<Boolean>> addTeacherTimes(@PathVariable("teacherId") @NotNull Long teacherId,
                                                                 @RequestBody @NotNull @Validated TeacherTimeVO teacherTimeVO) {
        Teacher teacher = teacherCRUD.getTeacherById(teacherId);

        if (teacher == null)
            throw ResourceNotFoundException.getInstance(TeacherMessage.getTeacherNotFound(teacherId.toString()));

        Plan plan = planCRUD.getPlanById(teacherTimeVO.getPlanId());

        if (plan == null)
            throw ResourceNotFoundException.getInstance(PlanMessage.getPlanNotFound(teacherTimeVO.getPlanId().toString()));

        teacherService.addTeacherTimes(teacher, plan, teacherTimeVO.getTimes());

        return ResponseEntity.ok(ResFact.<Boolean>build()
                .setResult(true)
                .setMessage(TeacherMessage.getAddTeacherTime())
                .get());
    }

    @GetMapping("/{teacherId}/time")
    public ResponseEntity<Result<TeacherTimeVO>> getTeacherTimes(@PathVariable("teacherId") @NotNull Long teacherId,
                                                                       @RequestParam(value = "planId") @NotNull Long planId) {
        Teacher teacher = teacherCRUD.getTeacherById(teacherId);

        if (teacher == null)
            throw ResourceNotFoundException.getInstance(TeacherMessage.getTeacherNotFound(teacherId.toString()));

        Plan plan = planCRUD.getPlanById(planId);

        if (plan == null)
            throw ResourceNotFoundException.getInstance(PlanMessage.getPlanNotFound(planId.toString()));

        return ResponseEntity.ok(ResFact.<TeacherTimeVO>build()
                .setResult(teacherService.getTeacherTimeVO(plan, teacherTimeCRUD.getTeacherTimes(plan, teacher)))
                .get());
    }
}