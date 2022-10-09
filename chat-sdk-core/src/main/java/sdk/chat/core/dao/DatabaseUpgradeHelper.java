package sdk.chat.core.dao;

import android.content.Context;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ben on 4/13/18.
 */

public class DatabaseUpgradeHelper extends DaoMaster.OpenHelper {

    // Legacy column names
    public final static Property LastMessageId = new Property(9, Long.class, "lastMessageId", false, "LAST_MESSAGE_ID");


    public DatabaseUpgradeHelper(Context context, String name) {
        super(context, name);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {

        List<Migration> migrations = getMigrations();

        // Only run migrations past the old version
        for (Migration migration : migrations) {
            if (oldVersion < migration.getVersion()) {
                migration.runMigration(db);
            }
        }
    }

    private List<Migration> getMigrations() {
        List<Migration> migrations = new ArrayList<>();
        migrations.add(new MigrationV2());
        migrations.add(new MigrationV3());
        migrations.add(new MigrationV4());
        migrations.add(new MigrationV6());
        migrations.add(new MigrationV7());
        migrations.add(new MigrationV8());
        migrations.add(new MigrationV9());
        migrations.add(new MigrationV10());
        migrations.add(new MigrationV11());
        migrations.add(new MigrationV12());
        migrations.add(new MigrationV13());
        migrations.add(new MigrationV14());
        migrations.add(new MigrationV15());
        migrations.add(new MigrationV16());
        migrations.add(new MigrationV17());
        migrations.add(new MigrationV18());
        migrations.add(new MigrationV19());
        migrations.add(new MigrationV20());
        migrations.add(new MigrationV21());
        migrations.add(new MigrationV22());
        migrations.add(new MigrationV23());
        migrations.add(new MigrationV24());
        migrations.add(new MigrationV25());
        migrations.add(new MigrationV26());
        migrations.add(new MigrationV27());
        migrations.add(new MigrationV28());
        migrations.add(new MigrationV29());
        migrations.add(new MigrationV30());

        // Sorting just to be safe, in case other people add migrations in the wrong order.
        Comparator<Migration> migrationComparator = (m1, m2) -> m1.getVersion().compareTo(m2.getVersion());
        Collections.sort(migrations, migrationComparator);

        return migrations;
    }

    private static class MigrationV2 implements Migration {

        @Override
        public Integer getVersion() {
            return 2;
        }

        @Override
        public void runMigration(Database db) {
            //Adding new table
            //UserDao.createTable(db, false);
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " ADD COLUMN " + MessageDao.Properties.NextMessageId.columnName + " LONG");
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " ADD COLUMN " + LastMessageId.columnName + " LONG");
        }
    }

    private static class MigrationV3 implements Migration {

        @Override
        public Integer getVersion() {
            return 3;
        }

        @Override
        public void runMigration(Database db) {
            //Adding new table

            ThreadMetaValueDao.createTable(db, true);
            UserMetaValueDao.createTable(db, true);
            db.execSQL("ALTER TABLE " + UserDao.TABLENAME + " DROP COLUMN " + "META_DATA");
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " DROP COLUMN " + "RESOURCES");

        }
    }

    private static class MigrationV4 implements Migration {

        @Override
        public Integer getVersion() {
            return 4;
        }

        @Override
        public void runMigration(Database db) {
            //Adding new table
            db.execSQL("ALTER TABLE " + UserDao.TABLENAME + " DROP COLUMN " + "LAST_UPDATED");
        }
    }

    private static class MigrationV6 implements Migration {

        @Override
        public Integer getVersion() {
            return 6;
        }

        @Override
        public void runMigration(Database db) {
            //Adding new table
            MessageMetaValueDao.createTable(db, true);

            ReadReceiptUserLinkDao.dropTable(db, false);
            ReadReceiptUserLinkDao.createTable(db, true);

            // Clear down the text database and re synchronize
            MessageDao.dropTable(db, false);
            MessageDao.createTable(db, true);
        }
    }

    private static class MigrationV7 implements Migration {

        @Override
        public Integer getVersion() {
            return 7;
        }

        @Override
        public void runMigration(Database db) {
            ReadReceiptUserLinkDao.dropTable(db, false);
            ReadReceiptUserLinkDao.createTable(db, true);
        }
    }

    private static class MigrationV8 implements Migration {
        @Override
        public Integer getVersion() {
            return 8;
        }

        @Override
        public void runMigration(Database db) {
            ThreadDao.dropTable(db, true);
            ThreadDao.createTable(db, true);
        }
    }

    private static class MigrationV9 implements Migration {
        @Override
        public Integer getVersion() {
            return 9;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " DROP COLUMN " + "LAST_MESSAGE_ID");
        }
    }

    private static class MigrationV10 implements Migration {
        @Override
        public Integer getVersion() {
            return 10;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " ADD COLUMN " + MessageDao.Properties.PreviousMessageId.columnName + " LONG");
        }
    }

    private static class MigrationV11 implements Migration {
        @Override
        public Integer getVersion() {
            return 11;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + UserDao.TABLENAME + " ADD COLUMN " + UserDao.Properties.IsOnline.columnName + " BOOLEAN");
        }
    }

    private static class MigrationV12 implements Migration {
        @Override
        public Integer getVersion() {
            return 12;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " ADD COLUMN " + ThreadDao.Properties.CanDeleteMessagesFrom.columnName + " INTEGER");
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " ADD COLUMN " + ThreadDao.Properties.LoadMessagesFrom.columnName + " INTEGER");
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " ADD COLUMN " + ThreadDao.Properties.Draft.columnName + " TEXT");
        }
    }

    private static class MigrationV13 implements Migration {
        @Override
        public Integer getVersion() {
            return 13;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadMetaValueDao.TABLENAME + " ADD COLUMN " + ThreadMetaValueDao.Properties.BooleanValue.columnName + " INTEGER");
            db.execSQL("ALTER TABLE " + ThreadMetaValueDao.TABLENAME + " ADD COLUMN " + ThreadMetaValueDao.Properties.IntegerValue.columnName + " INTEGER");
            db.execSQL("ALTER TABLE " + ThreadMetaValueDao.TABLENAME + " ADD COLUMN " + ThreadMetaValueDao.Properties.LongValue.columnName + " INTEGER");
            db.execSQL("ALTER TABLE " + ThreadMetaValueDao.TABLENAME + " ADD COLUMN " + ThreadMetaValueDao.Properties.FloatValue.columnName + " INTEGER");
        }
    }

    private static class MigrationV14 implements Migration {
        @Override
        public Integer getVersion() {
            return 14;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadMetaValueDao.TABLENAME + " DROP COLUMN " + "VALUE" + " INTEGER");
            db.execSQL("ALTER TABLE " + ThreadMetaValueDao.TABLENAME + " ADD COLUMN " + ThreadMetaValueDao.Properties.StringValue.columnName + " INTEGER");
        }
    }

    private static class MigrationV15 implements Migration {
        @Override
        public Integer getVersion() {
            return 15;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " DROP COLUMN " + "IMAGE_URL");
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " DROP COLUMN " + "NAME");
        }
    }

    private static class MigrationV16 implements Migration {
        @Override
        public Integer getVersion() {
            return 16;
        }

        @Override
        public void runMigration(Database db) {
            PublicKeyDao.createTable(db, true);
        }
    }
    private interface Migration {
        Integer getVersion();
        void runMigration(Database db);
    }

    private static class MigrationV17 implements Migration {
        @Override
        public Integer getVersion() {
            return 17;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " ADD COLUMN " + "USER_ACCOUNT_ID" + " TEXT");
        }
    }

    private static class MigrationV18 implements Migration {
        @Override
        public Integer getVersion() {
            return 18;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " RENAME TO old_table");
            ThreadDao.createTable(db, true);
            db.execSQL("INSERT INTO " + ThreadDao.TABLENAME + " SELECT * FROM old_table");
            db.execSQL("DROP TABLE old_table");
        }
    }

    private static class MigrationV19 implements Migration {
        @Override
        public Integer getVersion() {
            return 19;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " ADD COLUMN " + MessageDao.Properties.EncryptedText.columnName + " TEXT");
        }
    }

    private static class MigrationV20 implements Migration {
        @Override
        public Integer getVersion() {
            return 20;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " RENAME TO old_table");
            ThreadDao.createTable(db, true);
            db.execSQL("INSERT INTO " + ThreadDao.TABLENAME + " SELECT * FROM old_table");
            db.execSQL("DROP TABLE old_table");
        }
    }

    private static class MigrationV21 implements Migration {
        @Override
        public Integer getVersion() {
            return 21;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + MessageMetaValueDao.TABLENAME + " ADD COLUMN " + MessageMetaValueDao.Properties.IsLocal.columnName + " INTEGER");
        }
    }

    private static class MigrationV22 implements Migration {
        @Override
        public Integer getVersion() {
            return 22;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + MessageMetaValueDao.TABLENAME + " ADD COLUMN " + MessageMetaValueDao.Properties.Tag.columnName + " INTEGER");
        }
    }

    private static class MigrationV23 implements Migration {
        @Override
        public Integer getVersion() {
            return 23;
        }

        @Override
        public void runMigration(Database db) {
            CachedFileDao.createTable(db, true);
        }
    }

    private static class MigrationV24 implements Migration {
        @Override
        public Integer getVersion() {
            return 24;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + CachedFileDao.TABLENAME + " ADD COLUMN " + CachedFileDao.Properties.StartTime.columnName + " INTEGER");
        }
    }

    private static class MigrationV25 implements Migration {
        @Override
        public Integer getVersion() {
            return 25;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + CachedFileDao.TABLENAME + " ADD COLUMN " + CachedFileDao.Properties.FinishTime.columnName + " INTEGER");
        }
    }

    private static class MigrationV26 implements Migration {
        @Override
        public Integer getVersion() {
            return 26;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " ADD COLUMN " + ThreadDao.Properties.LastMessageId.columnName + " INTEGER");
            db.execSQL("ALTER TABLE " + ThreadDao.TABLENAME + " ADD COLUMN " + ThreadDao.Properties.LastMessageDate.columnName + " INTEGER");
        }
    }

    private static class MigrationV27 implements Migration {
        @Override
        public Integer getVersion() {
            return 27;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " ADD COLUMN " + MessageDao.Properties.IsRead.columnName + " INTEGER");
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " ADD COLUMN " + MessageDao.Properties.Reply.columnName + " TEXT");
        }
    }

    private static class MigrationV28 implements Migration {
        @Override
        public Integer getVersion() {
            return 28;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " ADD COLUMN " + MessageDao.Properties.PlaceholderPath.columnName + " TEXT");
            db.execSQL("ALTER TABLE " + MessageDao.TABLENAME + " ADD COLUMN " + MessageDao.Properties.FilePath.columnName + " TEXT");
        }
    }

    private static class MigrationV29 implements Migration {
        @Override
        public Integer getVersion() {
            return 29;
        }

        @Override
        public void runMigration(Database db) {
            String constraint = "IF NOT EXISTS ";

            // Thread User Link
            db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_USER_THREAD_LINK_USER_ID_THREAD_ID ON \"USER_THREAD_LINK\"" +
                    " (\"USER_ID\" ASC,\"THREAD_ID\" ASC);");

            // Read receipt link
            db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_READ_RECEIPT_USER_LINK_MESSAGE_ID_USER_ID ON \"READ_RECEIPT_USER_LINK\"" +
                    " (\"MESSAGE_ID\" ASC,\"USER_ID\" ASC);");

            // Message meta
            db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_MESSAGE_META_VALUE_MESSAGE_ID_KEY ON \"MESSAGE_META_VALUE\"" +
                    " (\"MESSAGE_ID\" ASC,\"KEY\" ASC);");

            // Thread meta
            db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_THREAD_META_VALUE_THREAD_ID_KEY ON \"THREAD_META_VALUE\"" +
                    " (\"THREAD_ID\" ASC,\"KEY\" ASC);");

            // User meta
            db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_USER_META_VALUE_USER_ID_KEY ON \"USER_META_VALUE\"" +
                    " (\"USER_ID\" ASC,\"KEY\" ASC);");

            // User thread link meta
            db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_USER_THREAD_LINK_META_VALUE_USER_THREAD_LINK_ID_KEY ON \"USER_THREAD_LINK_META_VALUE\"" +
                    " (\"USER_THREAD_LINK_ID\" ASC,\"KEY\" ASC);");

            // Contact link
            db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_CONTACT_LINK_LINK_OWNER_USER_DAO_ID_USER_ID ON \"CONTACT_LINK\"" +
                    " (\"LINK_OWNER_USER_DAO_ID\" ASC,\"USER_ID\" ASC);");

        }
    }

    private static class MigrationV30 implements Migration {
        @Override
        public Integer getVersion() {
            return 30;
        }

        @Override
        public void runMigration(Database db) {
            db.execSQL("ALTER TABLE " + CachedFileDao.TABLENAME + " ADD COLUMN " + CachedFileDao.Properties.Hash.columnName + " TEXT");
        }
    }


}
