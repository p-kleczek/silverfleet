package sfmainframe.board;


public enum Terrain {
	
	WATER(10), ISLAND(20), SHALLOW(30);
	
	private final int code;	// used to encode/decode data to/from the file
	
	
	private Terrain(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
