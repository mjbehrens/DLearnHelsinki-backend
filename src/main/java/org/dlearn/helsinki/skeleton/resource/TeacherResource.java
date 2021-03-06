package org.dlearn.helsinki.skeleton.resource;

import java.util.List;
import java.util.function.Function;

import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dlearn.helsinki.skeleton.exceptions.AddGroupFailedException;
import org.dlearn.helsinki.skeleton.exceptions.GroupClassMatchException;
import org.dlearn.helsinki.skeleton.exceptions.InvalidAgeException;
import org.dlearn.helsinki.skeleton.exceptions.PasswordException;

import org.dlearn.helsinki.skeleton.exceptions.StudentExistsException;
import org.dlearn.helsinki.skeleton.model.ChangePasswordStudent;
import org.dlearn.helsinki.skeleton.model.NewStudent;

import org.dlearn.helsinki.skeleton.model.Student;
import org.dlearn.helsinki.skeleton.model.Teacher;
import org.dlearn.helsinki.skeleton.model.ThemeAverage;
import org.dlearn.helsinki.skeleton.service.ChangePasswordService;
import org.dlearn.helsinki.skeleton.service.CreateNewUserService;
import org.dlearn.helsinki.skeleton.service.MoveToGroupService;
import org.dlearn.helsinki.skeleton.service.SecurityService;
import org.dlearn.helsinki.skeleton.service.StudentService;
import org.dlearn.helsinki.skeleton.service.TeacherStudentService;

@Path("/teachers")
public class TeacherResource {

    private final CreateNewUserService createNewUserService = new CreateNewUserService();
    private final ChangePasswordService change_password = new ChangePasswordService();
    private final SecurityService security = new SecurityService();
    private final TeacherStudentService teacherStudentService = new TeacherStudentService();
    private final MoveToGroupService moveToGroupService = new MoveToGroupService();
    private final StudentService studentService = new StudentService();

    /**
     * Request webapi/teachers/
     * @return teacher's info based on log credentials
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Teacher getTeacher() {
        return security.getTeacher().orElse(null);
    }

    // TODO delete. reason : function filled by /teachers/ GET
    /**
     * Request webapi/teachers/teacher_id
     * @param teacher_id
     * @return a teacher based on the id given.
     */
    //@GET
    //@Path("/{teacher_id}")
    //@Produces(MediaType.APPLICATION_JSON)
    public Teacher getTeacherFromId(@PathParam("teacher_id") int teacher_id) {
        return new Teacher(teacher_id, "username " + teacher_id, "password");
    }

    /**
     * Request webapi/teachers/teacher_id/classes
     * @param teacher_id
     * @return a teacher based on the id given.
     */
    @Path("/{teacher_id}/classes")
    public TeacherClassResource getClassesFromId(
            @PathParam("teacher_id") int teacher_id) {
        if (security.isTheTeacher(teacher_id)) {
            return new TeacherClassResource();
        } else {
            return null;
        }
    }

    /**
     * Create new student
     * @param teacher_id
     * @param student
     * @return
     * @throws RuntimeException
     * @throws InvalidAgeException 
     */
    @POST
    @Path("/{teacher_id}/create_student")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Student createNewStudent(@PathParam("teacher_id") int teacher_id,
            NewStudent student) throws RuntimeException, InvalidAgeException {
        Student returnedStudent = null;
        int student_id = student.student.get_id();
        int class_id = student.class_id;
        int group_id = student.group_id;
        try {
            if (teacherStudentService
                    .doesStudentIdExistInDatabase(student.student.get_id())) {
                // Student already exists, move him to a new group and class.
                if (moveToGroupService.moveStudentToGroup(class_id, student_id,
                        group_id)) {
                    returnedStudent = teacherStudentService
                            .getStudent(student_id);
                }

            } else {
                // Student does not exist, create a new student.
                returnedStudent = createNewUserService.createNewStudent(student)
                        .orElse(null);
            }

        } catch (StudentExistsException e) {
            throw new WebApplicationException(
                    Response.status(Status.BAD_REQUEST)
                            .entity("The student username is invalid or already exists in database. Choose another.")
                            .build());
        } catch (GroupClassMatchException e) {
            throw new WebApplicationException(
                    Response.status(Status.BAD_REQUEST)
                            .entity("The group doesn't correspond with the class.")
                            .build());
        } catch (AddGroupFailedException e) {
            // TODO: Prevent this
            throw new WebApplicationException(
                    Response.status(Status.BAD_REQUEST)
                            .entity("Adding student to group failed. Student was created without group.")
                            .build());

        } catch (PasswordException e) {
            throw new WebApplicationException(
                    Response.status(Status.BAD_REQUEST)
                            .entity("Invalid password.").build());
        }
        return returnedStudent;
    }

    /**
     * Used to change students password
     * @param teacher_id
     * @param student
     * @return
     * @throws PasswordException 
     */
    @POST
    @Path("/{teacher_id}/change_student_password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Student changeStudentPassword(
            @PathParam("teacher_id") int teacher_id,
            ChangePasswordStudent student) throws PasswordException {

        return security.getTeacher().map(new Function<Teacher, Student>() {
            @Override
            public Student apply(Teacher t) {
                try {
                    return change_password.changeStudentPassword(student);
                } catch (PasswordException ex) {
                    throw new WebApplicationException(
                            Response.status(Status.BAD_REQUEST)
                                    .entity("Invalid password.").build());
                }
            }
        }).orElse(null);
    }

    /**
     * get all students
     * @param teacher_id
     * @return 
     */
    @GET
    @Path("/{teacher_id}/students")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Student> getAllStudents(
            @PathParam("teacher_id") int teacher_id) {
        List<Student> students = null;
        if (security.isTheTeacher(teacher_id)) {
            students = teacherStudentService.getAllStudents();//(teacher_id);
        }

        return students;
    }

    /**
    * 
    * @param student_id
    * @return averages from all surveys from all students classes
    */
    @GET
    @Path("/{teacher_id}/survey_averages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ThemeAverage> getSurveyAverages(
            @QueryParam("student_id") int student_id) {
        return studentService.getSurveyAnswerAverages(student_id, 0, 0, 0);
    }
}
