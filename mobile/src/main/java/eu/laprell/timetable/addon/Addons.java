package eu.laprell.timetable.addon;

import eu.laprell.timetable.database.Teacher;
import eu.laprell.timetable.database.TimetableDatabase;

/**
 * Created by david on 05.12.14.
 */
public class Addons {
    public class Ids {
        public static final int ID_KREISGYMNASIUM_HEINSBERG = 1;
        public static final int ID_CORNELIUS_BURGH_GYMNASIUM_HEINSBERG = 2;
    }

    public static final int INDEX_FIRSTNAME = -1;
    public static final int INDEX_LASTNAME = -2;
    public static final int INDEX_PREFIX = -3;

    public static void insertTeachers(String[]teachersList, final int[]columnOrder,
                                      TimetableDatabase db) {
        final Teacher t = new Teacher(-1);

        for (int i = 0;i < teachersList.length;i++) {
            String[] data = teachersList[i].split("\\|");

            for (int j = 0; j < 3;j++) {
                if(columnOrder[j] == INDEX_PREFIX) {
                    t.setPrefix(data[j].length() == 0 ? null : data[j]);
                } else if(columnOrder[j] == INDEX_FIRSTNAME) {
                    t.setFirstName(data[j].length() == 0 ? null : data[j]);
                } else if(columnOrder[j] == INDEX_LASTNAME) {
                    t.setSecondName(data[j]);
                }
            }

            db.insertDatabaseEntryForId(t);
        }
    }
}
