package appeng.util;


/**
 * Created by Thiakil on 8/10/2017.
 */
public enum BaublesSlots
{
	AMULET_SLOT(0, 0, 0),
	RING1_SLOT(1, 0, 1),
	RING2_SLOT(2, 0, 2),
	BELT_SLOT(3, 0, 3),
	HEAD_SLOT(4, 1, 0),
	BODY_SLOT(5, 1, 1),
	CHARM_SLOT(6, 1, 2),
	;

	public static final int NUM_BAUBLE_SLOTS = values().length;
	public static final int BG_WIDTH = 51;
	public static final int BG_HEIGHT = 86;
	public static final int SLOT_SIZE = 18;
	public static final int X_GAP = 1;
	public static final int SLOT_START_X = 7;
	public static final int SLOT_START_Y = 7;
	public static final int BG_X_OFFSET = BaublesSlots.BG_WIDTH + 2;//subtracted
	public static final int BG_Y_OFFSET = SLOT_START_Y;//subtracted

	public int slotNum;
	public int offsetX;
	public int offsetY;

	BaublesSlots(int slotNum, int offsetX, int offsetY){
		this.slotNum = slotNum;
		this.offsetX = SLOT_START_X + (offsetX * (SLOT_SIZE+X_GAP));
		this.offsetY = /*SLOT_START_Y + */offsetY * SLOT_SIZE;
	}
}
