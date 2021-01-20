import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.swing.table.AbstractTableModel;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class Rooms extends AbstractTableModel {
    private DSLContext context;

    List<Room> rooms;

    public Rooms() throws SQLException {
        context = DSL.using(DriverManager.getConnection("jdbc:postgresql://localhost:5432/Hotel",
                "app", "passpassA@1"), SQLDialect.POSTGRES);

        pull();
    }

    public void pull() throws SQLException {
        rooms = context.select()
                .from(table("public.rooms"))
                .fetch().stream()
                .map(record ->
                        new Room((int) record.get("number"),
                                (int) record.get("people"),
                                (int) record.get("price")))
                .collect(Collectors.toList());
        fireTableDataChanged();
    }

    public void push() {
        var insert = context.insertInto(table("public.rooms"),
                field("number"),
                field("people"),
                field("price")
        );

        for (Room room : rooms) {
            insert = insert.values(
                    room.id,
                    room.people,
                    room.price
            );
        }

        insert.onConflict(field("number")).doUpdate()
                .set(field("people"), (Object) field("excluded.people"))
                .set(field("price"),(Object) field("excluded.price"));

        insert.execute();
    }

    int getMaxId() {
        return rooms.stream().mapToInt(Room::getId).max().orElse(-1);
    }

    @Override
    public int getRowCount() {
        return rooms.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> rooms.get(rowIndex).id;
            case 1 -> rooms.get(rowIndex).people;
            case 2 -> rooms.get(rowIndex).price;
            default -> throw new IllegalStateException("Unexpected value: " + columnIndex);
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0 -> rooms.get(rowIndex).id = Integer.parseInt((String) aValue);
            case 1 -> rooms.get(rowIndex).people = Integer.parseInt((String) aValue);
            case 2 -> rooms.get(rowIndex).price = Integer.parseInt((String) aValue);
            default -> throw new IllegalStateException("Unexpected value: " + columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public static class Room {
        int id;
        int people;
        int price;

        public Room(int id, int people, int price) {
            this.id = id;
            this.people = people;
            this.price = price;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getPeople() {
            return people;
        }

        public void setPeople(int people) {
            this.people = people;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }
    }
}
