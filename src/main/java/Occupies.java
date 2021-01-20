import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.swing.table.AbstractTableModel;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class Occupies extends AbstractTableModel {
    private DSLContext context;

    List<Occupied> occupied = List.of();

    public Occupies() throws SQLException {
        context = DSL.using(DriverManager.getConnection("jdbc:postgresql://localhost:5432/Hotel",
                "app", "passpassA@1"), SQLDialect.POSTGRES);

        pull();
    }

    public void pull() throws SQLException {
        occupied = context.select()
                .from(table("public.occupied"))
                .fetch().stream()
                .map(record ->
                        new Occupied((int) record.get("id"),
                                ((Date) record.get("date_begin")).toLocalDate(),
                                ((Date) record.get("date_end")).toLocalDate(),
                                (String) record.get("name"),
                                (int) record.get("room")
                        ))
                .collect(Collectors.toList());
        fireTableDataChanged();
    }

    public void push() {
        var insert = context.insertInto(table("public.occupied"),
                field("id"),
                field("date_begin"),
                field("date_end"),
                field("name"),
                field("room")
        );

        for (Occupied occupy : occupied) {
            insert = insert.values(
                    occupy.id,
                    Date.valueOf(occupy.from),
                    Date.valueOf(occupy.to),
                    occupy.by,
                    occupy.room
            );
        }

        insert.onConflict(field("id")).doUpdate()
                .set(field("date_begin"), (Object) field("excluded.date_begin"))
                .set(field("date_end"), (Object) field("excluded.date_end"))
                .set(field("name"), (Object) field("excluded.name"))
                .set(field("room"), (Object) field("excluded.room"));

        insert.execute();
    }

    int getMaxId() {
        return occupied.stream().mapToInt(Occupied::getId).max().orElse(-1);
    }

    @Override
    public int getRowCount() {
        return occupied.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> occupied.get(rowIndex).from;
            case 1 -> occupied.get(rowIndex).to;
            case 2 -> occupied.get(rowIndex).by;
            case 3 -> occupied.get(rowIndex).room;
            default -> throw new IllegalStateException("Unexpected value: " + columnIndex);
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0, 1 -> setDate(rowIndex, columnIndex, aValue);
            case 2 -> occupied.get(rowIndex).by = (String) aValue;
            case 3 -> occupied.get(rowIndex).room = Integer.parseInt((String) aValue);
            default -> throw new IllegalStateException("Unexpected value: " + columnIndex);
        }
    }

    void setDate(int row, int column, Object value) {

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public static class Occupied {
        int id;
        LocalDate from;
        LocalDate to;
        String by;
        int room;

        public Occupied(int id, LocalDate from, LocalDate to, String by, int room) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.by = by;
            this.room = room;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public LocalDate getFrom() {
            return from;
        }

        public void setFrom(LocalDate from) {
            this.from = from;
        }

        public LocalDate getTo() {
            return to;
        }

        public void setTo(LocalDate to) {
            this.to = to;
        }

        public String getBy() {
            return by;
        }

        public void setBy(String by) {
            this.by = by;
        }

        public int getRoom() {
            return room;
        }

        public void setRoom(int room) {
            this.room = room;
        }
    }
}
