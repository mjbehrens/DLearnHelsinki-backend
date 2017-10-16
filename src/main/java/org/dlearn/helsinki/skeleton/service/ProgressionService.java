
package org.dlearn.helsinki.skeleton.service;

import java.util.Collections;
import java.util.List;
import org.dlearn.helsinki.skeleton.database.Database;
import org.dlearn.helsinki.skeleton.model.ClassThemeAverage;
import org.dlearn.helsinki.skeleton.model.GroupThemeAverage;
import org.dlearn.helsinki.skeleton.model.ListClassThemeAverage;
import org.dlearn.helsinki.skeleton.model.StudentThemeAverage;

public class ProgressionService {
    private static final Database DB = new Database();
    
    public List<List<StudentThemeAverage>> getStudentProgression(int student_id, int amount) {
        return DB.getStudentThemeAverageProgression(student_id, amount).orElse(Collections.EMPTY_LIST);
    }
    
    public List<List<StudentThemeAverage>> getStudentClassProgression(int class_id, int student_id, int amount) {
        return DB.getStudentThemeAverageProgressionInClass(class_id, student_id, amount).orElse(Collections.EMPTY_LIST);
    }
    
    public List<List<GroupThemeAverage>> getGroupProgression(int class_id, int group_id, int amount) {
        return DB.getGroupThemeAverageProgression(class_id, group_id, amount).orElse(Collections.EMPTY_LIST);
    }
    
    public List<ListClassThemeAverage> getClassProgression(int class_id, int amount) {
        return DB.getClassThemeAverageProgression(class_id, amount).orElse(Collections.EMPTY_LIST);
    }
}
