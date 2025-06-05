package uk.ac.bris.cs.scotlandyard.model;

import java.io.Serializable;

import javax.annotation.Nonnull;

// Piece 接口代表的是游戏中的棋子，也就是参与者的角色
// 它定义了每个棋子它的颜色webColour()、身份（侦探isDetective()还是 Mr.X isMrX()）
// 有已经写好的实现类，每个侦探都有一个颜色（如红色、绿色、蓝色等），并且 isDetective() 方法始终返回 true
// MrX 只有一个实例，颜色是黑色，isDetective() 返回 false，表示它是 Mr.X。


// 进行实现接口 枚举实例是预定义的，不需要通过 new 关键字来创建实例。
/**
 * Represent a colored counter in the ScotlandYard game
 */
public interface Piece extends Serializable {
	/**
	 * @return the actual HTML colour hex of this piece as defined by the ScotlandYard game
	 */
	@Nonnull String webColour();
	/**
	 * @return true if this is a detective piece, false otherwise
	 */
	boolean isDetective();
	/**
	 * @return inverted {@link #isDetective()}
	 */
	default boolean isMrX() { return !isDetective();}

	/**
	 * Game-defined detective colour pieces.
	 */
	enum Detective implements Piece {
		RED("#f00"),
		GREEN("#0f0"),
		BLUE("#00f"),
		WHITE("#fff"),
		YELLOW("#ff0");
		private final String colour;
		Detective(String colour) {this.colour = colour;}
		@Nonnull @Override public String webColour() { return colour; }
		@Override public boolean isDetective() { return true; }
	}

	/**
	 * Game-defined MrX colour pieces.
	 */
	enum MrX implements Piece {
		MRX;
		@Override public boolean isDetective() { return false; }
		@Nonnull @Override public String webColour() { return "#000"; }
	}

}
