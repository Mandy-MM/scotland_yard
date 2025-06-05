package uk.ac.bris.cs.scotlandyard.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket;

// Mr.X 的旅行记录每一轮的记录都会保存在这个类中，它是“隐藏/公开位置”（Mr.X 用了票但是否告诉别人他的具体位置）
// 有两个构造方法，hidden()，reveal()，公开位置需要传入实际的 location 值，且不允许使用 HIDDEN -1作为 location。
// 封装：构造函数是private只能通过hidden()，reveal()来实例化对象
// 通过参数（ticket 和 location）来控制具体的行为（是“隐藏”还是“公开”），正是策略模式的应用。


/**
 * A POJO representing log entries of the MrX's travel log.
 * <br>
 * Use the static factory methods {@link #hidden(Ticket)} and {@link #reveal(Ticket, int)} to
 * create new instances.
 */
public final class LogEntry implements Serializable {
	private static final long serialVersionUID = -6468835796153329259L;
	// because Java's stupid Optional isn't intend to be used as a field...
	private static final int HIDDEN = -1;
	private final Ticket ticket;
	private final int location;
	/**
	 * @param ticket the ticket used in this entry
	 * @return a log entry of a hidden round for Mrx
	 */
	public static LogEntry hidden(
			@Nonnull Ticket ticket) { return new LogEntry(ticket, HIDDEN); }
	/**
	 * @param ticket the ticket used in this entry
	 * @param location the location MrX is at during this reveal round
	 * @return a log entry of a reveal round for Mrx
	 */
	public static LogEntry reveal(@Nonnull Ticket ticket, int location) {
		if (location == HIDDEN) throw new IllegalArgumentException();
		return new LogEntry(ticket, location);
	}
	private LogEntry(@Nonnull Ticket ticket, int location) {
		this.ticket = Objects.requireNonNull(ticket);
		this.location = location;
	}
	/**
	 * @return the ticket in this log entry
	 */
	public Ticket ticket() { return ticket; }
	/**
	 * @return the location in this log entry, empty means MrX's location is hidden
	 */
	public Optional<Integer> location() {
		return location == HIDDEN ? Optional.empty() : Optional.of(location);
	}
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LogEntry logEntry = (LogEntry) o;
		return location == logEntry.location && ticket == logEntry.ticket;
	}
	@Override public int hashCode() { return Objects.hash(ticket, location); }
}
