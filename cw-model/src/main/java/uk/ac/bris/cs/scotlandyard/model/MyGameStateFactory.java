

package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Piece.Detective;
import uk.ac.bris.cs.scotlandyard.model.Piece.MrX;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Move.SingleMove;
import uk.ac.bris.cs.scotlandyard.model.Move.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport;

import java.util.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */

//用 final 修饰，说明它们一旦设定就不能改了（immutable）
public final class MyGameStateFactory implements Factory<GameState> {
/*
	这段是在实现Factory接口，接口中规定必须创建一个gamestate类型的对象，MyGameStateFactory
	这个类实现了接口，	实例化了GameState，GameState extends Board并且增加了一个advance方法
	参数：
	GameSetup setup：包含了mrx第几轮现身（ImmutableList<Boolean> moves），一个图结构，
	表示哪些站之间可以连接，以及可以使用哪些交通工具连接（比如 TAXI、BUS）
（ImmutableValueGraph<Integer, ImmutableSet<Transport>> graphs）这两个信息在这里
	没有实例化就是没有具体数据信息

	Player mrX：包含这是什么棋子（MRX）(Piece piece)，起始位置是 45(location)，有多少票数如
	（taxi 4 张，bus 3 张，...)(ImmutableMap<Ticket, Integer> tickets) 这些信息定义了玩家的信息结构

	ImmutableList<Player> detectives：一个包含所有侦探的不可变列表。

	@Override 是用来表明我们实现了接口中的一个方法。
 */
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		return new MyGameState(setup, mrX, detectives);
	}



	//GameState接口 实际上是 Board 接口的一个扩展，并增加了 advance 方法，
	// 这个方法在游戏中用来根据玩家的移动生成一个新的游戏状态。
	// GameState 是对当前游戏状态的具体表示，而 Board 则更像是游戏环境的一部分，包含了更多的通用信息。

/* Board 接口包含的方法：getSetup()看地图规则, getPlayers()谁在玩, getDetectiveLocation()侦探在哪,
	getPlayerTickets()玩家多少票， getMrXTravelLog()Mr.X 的行动日志， getWinner()谁赢了，
	getAvailableMoves()返回当前所有玩家可以执行的合法动作（比如去哪儿、用什么票）

	MyGameState这个类是实现gamestate接口的，gamestate接口又是board接口的extend
 */
	private static final class MyGameState implements GameState {
		private final GameSetup setup; //保存了游戏的初始设置move和graph那个
		private final ImmutableSet<Piece> remaining; //仍然可移动的玩家
		// 有一个可移动玩家列表，每个列表中的元素都是piece类的有颜色和身份信息isdetective（）
		private final ImmutableList<LogEntry> log; // MrX's travel log
	// 当前回合x的行动（是否隐藏）组成的列表，每个元素是LogEntry type

		ImmutableMap<Piece, ImmutableSet<Move>> movesMap;
	// 保存每个玩家（Piece）的合法移动。movesMap 将每个玩家与他们的合法移动集合关联起来。比如：mrX 可能有 Single 或 Double
		private final ImmutableSet<Piece> winner; // Current winner of the game
		private final int currentRound; // Current round number
		private final Player mrXPlayer;
		//piece是棋子，player是每个玩家，每个玩家都有一个棋子
		private final ImmutableList<Player> detectivePlayers;
	// 所有侦探玩家的列表


//这段是MyGameState类的构造函数，有一系列的异常检查来确定传递的参数是不是异常，setup，mrX，detectives
// 是在构造时要输入，其他是内部进行生成
		private MyGameState(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {

			//检查setup，mrX，detectives是不是空，如果空就报错
			if (setup == null || mrX == null || detectives == null)
				throw new NullPointerException("NullPointer");

			//检查 setup 中的 moves 列表（MrX 的移动列表是否为空）是否为空，如果为空，表示游戏规则有问题就报错。
			//查图中是否有任何站点（node）。如果图为空，游戏无法进行，报错。
			if (setup.moves.isEmpty() || setup.graph.nodes().isEmpty())
				throw new IllegalArgumentException("IllegalArgument");

			//hashset 用于存储一组元素，并保证集合中的每个元素都是唯一的
			//用HashSet构造函数实例化了就可以用它的方法了
			Set<Integer> detectiveLocations = new HashSet<>();
			//
			for (Player detective : detectives) {
				//检查每个侦探是否持有 DOUBLE 或 SECRET 票证,有的话就证明游戏错误了
				if (detective.has(ScotlandYard.Ticket.DOUBLE) || detective.has(ScotlandYard.Ticket.SECRET)) {
					throw new IllegalArgumentException("IllegalArgument");
				}
				//确保每个侦探的位置是唯一的，add() 方法返回 false 如果该位置已经存在（即两个侦探在同一个位置）
				//add 方法是 HashSet 类的一部分
				if (!detectiveLocations.add(detective.location())) {
					throw new IllegalArgumentException("IllegalArgument");
				}
			}

			this.setup = setup;
			this.mrXPlayer = mrX;
			this.detectivePlayers = detectives;
			this.remaining = ImmutableSet.of(mrX.piece());
			//最开始只有mrX可以动

			this.currentRound = 0;
			this.log = ImmutableList.of();    // Log is empty at the beginning
			//Google Guava 库中的一个方法，用于创建一个空的不可变列表，ImmutableList无法在其上添加、删除或修改元素

			this.movesMap  = buildMovesMap();
			//负责生成每个玩家的可用移动，是在MyGameState的方法，现在还没写，在下面helper里

			this.winner = calculateWinner(this);  // Determine winner based on conditions
			if(!winner.isEmpty()){
				System.out.println("mrX win in init step");
				this.movesMap  = ImmutableMap.of();
			}


		}

//有两个构造函数，根据传递参数的不同来确定用哪个（Constructor Overloading，这个自由度更高
		private MyGameState(
				GameSetup setup,
				Player mrX,
				ImmutableList<Player> detectives,
				int currentRound,
				ImmutableList<LogEntry> log,
				ImmutableSet<Piece> winner,
				ImmutableSet<Piece> remaining
		) {

			// Parameter check
			if (setup == null || mrX == null || detectives == null)
				throw new NullPointerException("NullPointer in MyGameState constructor");
			if (setup.moves.isEmpty() || setup.graph.nodes().isEmpty())
				throw new IllegalArgumentException("Invalid setup: empty moves or graph");
			Set<Integer> detectiveLocations = new HashSet<>();
			for (Player d : detectives) {
				if (d.has(Ticket.DOUBLE) || d.has(Ticket.SECRET)) {
					throw new IllegalArgumentException("Detectives cannot hold DOUBLE or SECRET tickets");
				}
				if (!detectiveLocations.add(d.location())) {
					throw new IllegalArgumentException("Two detectives in same location!");
				}
			}

			this.setup = setup;
			this.mrXPlayer = mrX;
			this.detectivePlayers = detectives;
			this.currentRound = currentRound;
			this.log = log;
			this.winner = winner;
			this.remaining = remaining;

			if (!winner.isEmpty()) {
				this.movesMap  = ImmutableMap.of();
			} else {
				this.movesMap  = buildMovesMap();
			}
		}

/*
	Board 接口包含的方法：getSetup()看地图规则, getPlayers()谁在玩, getDetectiveLocation()侦探在哪,
	getPlayerTickets()玩家多少票， getMrXTravelLog()Mr.X 的行动日志， getWinner()谁赢了，
	getAvailableMoves()返回当前所有玩家可以执行的合法动作（比如去哪儿、用什么票）
 */

		// Return the current game setup看地图规则
		@Nonnull @Override
		public GameSetup getSetup() {
			return setup;
		}

		// Return all remaining players谁在玩
		@Nonnull @Override
		public ImmutableSet<Piece> getPlayers() {
			return remaining;
		}



		// Return the location of the given detective

	/*
	Optional 是一个容器对象，它用于表示一个值可能存在，也可能为 null空，这样写表示侦探可能不存在
	Optional 的使用 使得返回值更加安全，避免直接返回 null，并且调用者可以明确知道返回值是“存在”还是“不存在”的状态。

	输入Detective detective内容只有颜色和是否是侦探，
	这个方法是找侦探在哪，Detective类那个emun类
	 */
		@Nonnull @Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			//找plaer列表中所有侦探玩家，遍历列表每个对象匹配是否是侦探，如果是就进入循环
			for (Player player : detectivePlayers) {
				// 检查要获取位置的当前玩家的棋子是否与本次循环传入的 detective 对象相同
				if (player.piece().equals(detective)) {
					return Optional.of(player.location()); //Optional.of() 包装该位置并返回
					//创建一个包含单个元素的 Optional 对象，对的就是不new也能创
				}
			}
			return Optional.empty();
		}


		// Return MrX's travel logMr.X 的行动日志
		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}



		// Return all available moves for the current player
		@Nonnull
		@Override

		//这个方法是根据当前是谁的回合（mrX 或侦探），它会返回对应的可用移动集合。

		public ImmutableSet<Move> getAvailableMoves() { 	//移动集合
			//没有赢家，返回空
			if (!winner.isEmpty()) return ImmutableSet.of();
			//mrX回合，返回
			if (mrXTurn()) {
				//从movesMap（包含ImmutableSet<Move>）中获得mrX的可用移动getOrDefault有就创建mrXPlayer.piece对应的键的移动，没有就空列表
				return movesMap.getOrDefault(mrXPlayer.piece(), ImmutableSet.of());
			}else{
				return getdetectivesMove();
			}
		}


		// Get all available moves for remaining detectives
	//获取侦探移动，
		ImmutableSet<Move> getdetectivesMove(){
			// Builder<Move> 是 ImmutableSet 的内部类，用来创建 ImmutableSet 实例
			ImmutableSet.Builder<Move> combined = ImmutableSet.builder();
			// 遍历所有list里的侦探
			for (Player detective : detectivePlayers) {
				//是不是仍然可移动的玩家？
				if (remaining.contains(detective.piece())) {
					//把当前侦探的移动加到combined实例的列表中
					combined.addAll(movesMap.getOrDefault(detective.piece(), ImmutableSet.of()));
				}
			}

			//build() 是 ImmutableSet.Builder 中的方法，它将你通过 add() 或 addAll() 方法添加到构建器中的元素转换为一个不可变集合。
			//当你调用 build() 时，构造函数会生成一个不可变Set，这个集合一旦创建后是不可变的，不能再添加或删除元素。
			//这个set里都是侦探的移动
			return combined.build();
		}


		// Return the winner of the game		@Nonnull 谁赢了
		@Override
		public ImmutableSet<Piece> getWinner() {
			return winner;
		}


		@Nonnull
		@Override
		// 玩家的票
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			// Find the corresponding Player
			Player player = findPlayer(piece); //findplayer后面写的方法
			//没有玩家就没票
			if (player == null) return Optional.empty();

			return Optional.of(ticket -> player.tickets().getOrDefault(ticket, 0));
			//Lambda 表达式：更简洁地实现接口
			//该作用是：给定一个票证 ticket，返回该票证的数量，如果没有该票证，返回 0。
			//
		}



		// Handle a player's move and return the new game state
		@Nonnull
		@Override
		//GameState新增的方法，不在board里，作用是处理玩家的每次移动，并根据移动是MrX 或侦探返回新的游戏状态。
		public GameState advance(Move move) {
			// commencedBy() 是 Move 类中的一个方法，这个方法用于返回进行该移动的玩家
			// 看这个移动的玩家是谁
			if (move.commencedBy().isMrX()) {
				//后面写的方法
				return handleMrXMove(move);
			} else {
				//后面写的方法
				return handleDetectiveMove(move);
			}
		}
//周三work done

		// ============ Helper Methods ============

	//在构造函数里被调用，用来生成每个角色和他可用的移动，不可改Map类型
		private ImmutableMap<Piece, ImmutableSet<Move>> buildMovesMap() {
			ImmutableMap.Builder<Piece, ImmutableSet<Move>> builder = ImmutableMap.builder();

			// All available moves for MrX
			builder.put(mrXPlayer.piece(), calculateAvailableMovesForMrX(this));

			// Calculate available moves for each detective separately
			for (Player detective : detectivePlayers) {
				if(!remaining.contains(detective.piece()))
					continue;
				builder.put(detective.piece(), calculateAvailableMovesForDetective(detective));
			}

			return builder.build();
		}


		private ImmutableSet<Move> calculateAvailableMovesForMrX(MyGameState myGameState) {
			ImmutableSet.Builder<Move> availableMoves = ImmutableSet.builder();

			// Get Mr. X's current ticket information
			ImmutableMap<Ticket, Integer> mrXTickets = myGameState.mrXPlayer.tickets();

			// Calculate Mr. X's single moves
			for (int destination : myGameState.setup.graph.adjacentNodes(myGameState.mrXPlayer.location())) {
				if (detectiveInLocation(destination)) {
					continue; // Skip if the destination is occupied by a detective
				}
				for (Transport transport : myGameState.setup.graph.edgeValueOrDefault(myGameState.mrXPlayer.location(), destination, ImmutableSet.of())) {
					Ticket ticketRequired = transport.requiredTicket();

					// Check if Mr. X has enough of the required ticket
					if (mrXTickets.getOrDefault(ticketRequired, 0) > 0) {
						availableMoves.add(new SingleMove(myGameState.mrXPlayer.piece(), myGameState.mrXPlayer.location(), ticketRequired, destination));
					}
					if (mrXTickets.getOrDefault(Ticket.SECRET, 0) > 0) {
						availableMoves.add(new SingleMove(
								myGameState.mrXPlayer.piece(),
								myGameState.mrXPlayer.location(),
								Ticket.SECRET,
								destination
						));
					}
				}
			}

			int remainingRounds = myGameState.setup.moves.size() - myGameState.currentRound;
			// // Calculate Mr. X's double moves (if he has a Double Ticket)
			if (mrXTickets.getOrDefault(Ticket.DOUBLE, 0) > 0 && remainingRounds >= 2)  {
				for (int firstDestination : myGameState.setup.graph.adjacentNodes(myGameState.mrXPlayer.location())) {
					if (detectiveInLocation(firstDestination)) continue;

					for (Transport firstTransport : myGameState.setup.graph.edgeValueOrDefault(myGameState.mrXPlayer.location(), firstDestination, ImmutableSet.of())) {
						Ticket firstTicketRequired = firstTransport.requiredTicket();
						List<Ticket> firstStepTickets = new ArrayList<>();

						if (mrXTickets.getOrDefault(firstTicketRequired, 0) > 0) {
							firstStepTickets.add(firstTicketRequired);
						}
						if (mrXTickets.getOrDefault(Ticket.SECRET, 0) > 0) {
							firstStepTickets.add(Ticket.SECRET);
						}

						if (firstStepTickets.isEmpty()) continue;
						for (int secondDestination : myGameState.setup.graph.adjacentNodes(firstDestination)) {
							if (detectiveInLocation(secondDestination)) continue;
							for (Transport secondTransport : myGameState.setup.graph.edgeValueOrDefault(firstDestination, secondDestination, ImmutableSet.of())) {
								Ticket secondRequired = secondTransport.requiredTicket();

								List<Ticket> secondStepTickets = new ArrayList<>();

								if (mrXTickets.getOrDefault(secondRequired, 0) > 0) {
									secondStepTickets.add(secondRequired);
								}

								if (mrXTickets.getOrDefault(Ticket.SECRET, 0) > 0) {
									secondStepTickets.add(Ticket.SECRET);
								}

								for (Ticket t1 : firstStepTickets) {
									for (Ticket t2 : secondStepTickets) {
										int secretNeeded = 0;
										if (t1 == Ticket.SECRET) secretNeeded++;
										if (t2 == Ticket.SECRET) secretNeeded++;

										if (t1 == t2 && mrXTickets.getOrDefault(t1, 0) < 2)
											continue;

										if (secretNeeded <= mrXTickets.getOrDefault(Ticket.SECRET, 0)) {
											availableMoves.add(new DoubleMove(
													myGameState.mrXPlayer.piece(),
													myGameState.mrXPlayer.location(),
													t1, firstDestination,
													t2, secondDestination
											));
										}

									}

								}
							}

						}




					}
				}
			}

			return availableMoves.build();
		}

		private ImmutableSet<Move> calculateAvailableMovesForDetective(Player detective) {
			ImmutableSet.Builder<Move> moves = ImmutableSet.builder();

			// If there is already a winner, return an empty set
			if (!winner.isEmpty()) {
				return ImmutableSet.of();
			}

			ImmutableMap<Ticket, Integer> tickets = detective.tickets();
			int source = detective.location();

			// Single moves
			for (int destination : setup.graph.adjacentNodes(source)) {
				// Detectives cannot move to a location already occupied by another detective
				boolean isOccupiedByOtherDetective = false;
				for (Player otherDetective : detectivePlayers) {
					if (otherDetective != detective && otherDetective.location() == destination) {
						isOccupiedByOtherDetective = true;
						break;
					}
				}

				if (isOccupiedByOtherDetective) {
					continue;
				}

				// Check if the detective has enough tickets to move
				for (Transport transport : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
					Ticket required = transport.requiredTicket();
					if (tickets.getOrDefault(required, 0) > 0) {
						moves.add(new SingleMove(detective.piece(), source, required, destination));
					}
				}
			}

			return moves.build();
		}

		private ImmutableSet<Move> calculateAvailableMovesForDetectives(MyGameState myGameState) {
			ImmutableSet.Builder<Move> availableMoves = ImmutableSet.builder();

			// For each detective, calculate all valid single moves
			for (Player detective : myGameState.detectivePlayers) {
				if (!myGameState.remaining.contains(detective.piece())) {
					continue;
				}
				// Get ticket information for detectives
				ImmutableMap<Ticket, Integer> detectiveTickets = detective.tickets();

				// calculate single moves for detective
				for (int destination : myGameState.setup.graph.adjacentNodes(detective.location())) {
					boolean isOccupiedByOtherDetective = false;
					for (Player otherDetective : myGameState.detectivePlayers) {
						if (otherDetective != detective && otherDetective.location() == destination) {
							isOccupiedByOtherDetective = true;
							break;
						}
					}

					if (isOccupiedByOtherDetective) {
						continue;  // Skip destination occupied by another detective
					}

					for (Transport transport : myGameState.setup.graph.edgeValueOrDefault(detective.location(), destination, ImmutableSet.of())) {
						Ticket ticketRequired = transport.requiredTicket();

						// Determine if the detective has enough tickets to make the move
						if (detectiveTickets.getOrDefault(ticketRequired, 0) > 0) {
							availableMoves.add(new SingleMove(detective.piece(), detective.location(), ticketRequired, destination));
						}
					}
				}
			}

			return availableMoves.build();
		}

		private ImmutableSet<Move> calculateAvailableMovesForDetectivesWithoutRemaining(MyGameState myGameState){
			ImmutableSet.Builder<Move> availableMoves = ImmutableSet.builder();

			// In the case of a detective, calculate the legal location to which it can be moved
			for (Player detective : myGameState.detectivePlayers) {
				// get ticket information for detective
				ImmutableMap<Ticket, Integer> detectiveTickets = detective.tickets();

				// calculate single move for detectives
				for (int destination : myGameState.setup.graph.adjacentNodes(detective.location())) {
					boolean isOccupiedByOtherDetective = false;
					for (Player otherDetective : myGameState.detectivePlayers) {
						if (otherDetective != detective && otherDetective.location() == destination) {
							isOccupiedByOtherDetective = true;
							break;
						}
					}

					if (isOccupiedByOtherDetective) {
						continue;  // Skip destination occupied by another detective
					}

					for (Transport transport : myGameState.setup.graph.edgeValueOrDefault(detective.location(), destination, ImmutableSet.of())) {
						Ticket ticketRequired = transport.requiredTicket();

						// check weather detective have enough ticket
						if (detectiveTickets.getOrDefault(ticketRequired, 0) > 0) {
							availableMoves.add(new SingleMove(detective.piece(), detective.location(), ticketRequired, destination));
						}
					}
				}
			}

			return availableMoves.build();
		}

		private ImmutableSet<Piece> calculateWinner(MyGameState myGameState) {
			System.out.println("#" + myGameState);
			// Check if MrX has been caught
			for (Player detective : myGameState.detectivePlayers) {
				if (detective.location() == myGameState.mrXPlayer.location()){
					System.out.println("detectives win cause of catch mrx");
					return detectivesWin();
				}

			}

			if(myGameState.log.size() >= myGameState.setup.moves.size()){
				return mrXWin();
			}

			boolean allDetectivesStuck = false;
			if (calculateAvailableMovesForDetectives(myGameState).isEmpty() && (!myGameState.mrXTurn())) {
				allDetectivesStuck = true;
			}
			if(allDetectivesOutOfTickets(myGameState)){
				allDetectivesStuck = true;
			}
			if(calculateAvailableMovesForDetectivesWithoutRemaining(myGameState).isEmpty() && myGameState.currentRound == 0){
				allDetectivesStuck = true;
			}


			boolean mrxStuck = false;
			if(calculateAvailableMovesForMrX(myGameState).isEmpty() && myGameState.mrXTurn())
				mrxStuck = true;

			if(isMrXBlocked(myGameState)){
				mrxStuck = true;
			}

			if(mrxStuck){
				System.out.println("detectives win cause of mrxStuck");
				System.out.println(myGameState);
				return detectivesWin();
			}

			if (allDetectivesStuck){
				System.out.println("mrXWin cause of allDetectivesStuck");
				System.out.println("extra output in calculateWinner:" + myGameState);
				return mrXWin();
			}


			// If no win conditions are met, continue the game with no winner yet
			return ImmutableSet.of();
		}

		private boolean allDetectivesOutOfTickets(MyGameState myGameState) {
			for (Player detective : myGameState.detectivePlayers) {
				if (detective.has(Ticket.TAXI) ||
						detective.has(Ticket.BUS) ||
						detective.has(Ticket.UNDERGROUND)) {
					return false;
				}
			}
			return true;
		}

		private boolean mrXTurn(){
			return currentRound % 2 == 0;
		}

		private boolean DetectiverTrun(){
			return currentRound % 2 == 1;
		}

		private Player findPlayer(Piece piece) {
			if (mrXPlayer.piece().equals(piece)) {
				return mrXPlayer;
			}
			for (Player d : detectivePlayers) {
				if (d.piece().equals(piece)) {
					return d;
				}
			}
			return null;
		}


		private MyGameState nextState(
				Player newMrX,
				ImmutableList<Player> newDetectives,
				int nextRound,
				ImmutableList<LogEntry> newLog,
				ImmutableSet<Piece> newWinner,
				ImmutableSet<Piece> newremaining) {


			ImmutableSet<Piece> filteredRemaining = newremaining.stream()
					.filter(p -> {
						Player detective = findPlayer(p);  // Get the corresponding Player
						if (detective != null && detective.piece().equals(p)) {
							// Skip this detective if they have no available moves
							ImmutableSet<Move> availableMoves = movesMap.getOrDefault(detective.piece(), ImmutableSet.of());
							return !availableMoves.isEmpty();
						}
						return true;
					})
					.collect(ImmutableSet.toImmutableSet());


			if(DetectiverTrun()){
				if (filteredRemaining.isEmpty()) {
					nextRound++;
					filteredRemaining = ImmutableSet.of(newMrX.piece());
				}
			}else{
				nextRound++;
				ImmutableSet.Builder<Piece> builder = ImmutableSet.builder();
				for (Player d : detectivePlayers) builder.add(d.piece());
				filteredRemaining = builder.build();
			}


			MyGameState newState =  new MyGameState(
					this.setup,
					newMrX,
					newDetectives,
					nextRound,
					newLog,
					newWinner,
					filteredRemaining
			);

			ImmutableSet<Piece> winner = calculateWinner(newState);
			if(winner.isEmpty())
				return newState;
			else{
				return new MyGameState(
						this.setup,
						newMrX,
						newDetectives,
						nextRound,
						newLog,
						winner,
						filteredRemaining
				);
			}


		}

		private GameState handleMrXMove(Move move) {
			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(SingleMove m) {
					return doMrXSingleMove(m);
				}
				@Override
				public GameState visit(DoubleMove m) {
					return doMrXDoubleMove(m);
				}
			});
		}

		private boolean isMrXBlocked(MyGameState myGameState) {
			int mrXLocation = myGameState.mrXPlayer.location();

			// Get all adjacent nodes MrX can move to
			Set<Integer> possibleDestinations = myGameState.setup.graph.adjacentNodes(mrXLocation);

			// Check if all adjacent nodes are occupied by detectives
			for (int destination : possibleDestinations) {
				if (!isOccupiedByDetective(myGameState, destination)) {
					return false;
				}
			}

			return true;
		}


		private boolean isOccupiedByDetective(MyGameState myGameState, int location) {
			for (Player detective : myGameState.detectivePlayers) {
				if (detective.location() == location) return true;
			}
			return false;
		}

		private GameState doMrXSingleMove(SingleMove m) {
			// Deduct the used ticket
			Player updatedMrX = mrXPlayer.use(m.ticket);
			// Move to the new location
			updatedMrX = updatedMrX.at(m.destination);


			// Reveal rule
			boolean reveal = setup.moves.get(currentRound);
			ImmutableList<LogEntry> updatedLog = updateLogForMrX(log, m.ticket, m.destination, reveal);

			// MrX wins if he finishes all rounds
			if (updatedLog.size() >= setup.moves.size()) {
				System.out.println("mrXWin cause of MrX get maxPath");
				return new MyGameState(
						setup,
						updatedMrX,
						detectivePlayers,
						currentRound,
						updatedLog,
						mrXWin(),
						remaining
				);
			}

			// Game continues
			return nextState(
					updatedMrX,
					detectivePlayers,
					currentRound,
					updatedLog,
					ImmutableSet.of(),
					remaining
			);
		}

		private GameState doMrXDoubleMove(DoubleMove m) {
			// Deduct tickets: m.ticket1, m.ticket2, and DOUBLE
			Player updatedMrX = mrXPlayer.use(m.ticket1).use(m.ticket2).use(Ticket.DOUBLE);

			// First move
			updatedMrX = updatedMrX.at(m.destination1);

			// Update log after first move
			boolean reveal = setup.moves.get(currentRound);
			ImmutableList<LogEntry> logAfterFirst = updateLogForMrX(log, m.ticket1, m.destination1, reveal);
			// Second move
			updatedMrX = updatedMrX.at(m.destination2);
			ImmutableList<LogEntry> finalLog = updateLogForMrX(logAfterFirst, m.ticket2, m.destination2, reveal);

			// If MrX finishes all rounds after double move, he wins
			if (finalLog.size() >= setup.moves.size()) {
				System.out.println("mrXWin cause of MrX get maxPath");
				return new MyGameState(
						setup,
						updatedMrX,
						detectivePlayers,
						currentRound,
						finalLog,
						mrXWin(),
						remaining
				);
			}

			// Game continues
			return nextState(
					updatedMrX,
					detectivePlayers,
					currentRound,
					finalLog,
					ImmutableSet.of(),
					remaining
			);
		}

		private GameState handleDetectiveMove(Move move) {
			// Find the detective player who makes the move
			Player detective = findPlayer(move.commencedBy());
			if (detective == null) throw new IllegalStateException("Cannot find detective player");

			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(SingleMove m) {
					return doDetectiveSingleMove(detective, m);
				}

				@Override
				public GameState visit(DoubleMove m) {
					throw new IllegalArgumentException("Detective cannot double-move");
				}
			});
		}

		private GameState doDetectiveSingleMove(Player detective, SingleMove m) {
			// Deduct the ticket from the detective
			Player updatedDetective = detective.use(m.ticket);
			// Move the detective to the destination
			updatedDetective = updatedDetective.at(m.destination);
			// Transfer the used ticket to MrX
			Player updatedMrX = mrXPlayer.give(m.ticket);

			ImmutableSet<Piece> newRemaining = this.remaining.stream()
					.filter(p-> !p.equals(detective.piece())).collect(ImmutableSet.toImmutableSet());


			// Replace the detective with updated info
			List<Player> newDetectives = new ArrayList<>(detectivePlayers);
			for (int i = 0; i < newDetectives.size(); i++) {
				if (newDetectives.get(i).piece().equals(detective.piece())) {
					newDetectives.set(i, updatedDetective);
					break;
				}
			}
			ImmutableList<Player> updatedDetectives = ImmutableList.copyOf(newDetectives);


			// If detective catches MrX => detectives win
			if (updatedDetective.location() == mrXPlayer.location()) {
				System.out.println("detectives win cause of catch mrx");
				return new MyGameState(
						setup,
						updatedMrX,
						updatedDetectives,
						currentRound,
						log,
						detectivesWin(),
						newRemaining
				);
			}

			// Continue the game
			return nextState(
					updatedMrX,
					updatedDetectives,
					currentRound,
					log,
					ImmutableSet.of(),
					newRemaining
			);
		}


		private boolean detectiveInLocation(int location) {
			for (Player d : detectivePlayers) {
				if (d.location() == location) return true;
			}
			return false;
		}

		private ImmutableList<LogEntry> updateLogForMrX(ImmutableList<LogEntry> oldLog, Ticket t, int location, boolean reveal) {
			List<LogEntry> newLog = new ArrayList<>(oldLog);
			if (reveal) {
				newLog.add(LogEntry.reveal(t, location));
			} else {
				newLog.add(LogEntry.hidden(t));
			}
			return ImmutableList.copyOf(newLog);
		}

		private ImmutableSet<Piece> detectivesWin() {
			ImmutableSet.Builder<Piece> builder = ImmutableSet.builder();
			for (Player d : detectivePlayers) {
				builder.add(d.piece());
			}
			return builder.build();
		}


		private ImmutableSet<Piece> mrXWin() {
			return ImmutableSet.of(mrXPlayer.piece());
		}

		@Override
		public String toString() {
			return "MyGameState{" +
					"currentRound=" + currentRound +
					", mrXPlayer=" + mrXPlayer +
					", detectivePlayers=" + detectivePlayers +
					", log=" + log +
					", movesMap=" + movesMap +
					", winner=" + winner +
					", remaining=" + remaining +
					'}';
		}


	}
}
