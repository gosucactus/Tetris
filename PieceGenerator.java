import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PieceGenerator {
    private List<Integer> currentBag;
    private static final int BAG_SIZE = 7;

    public PieceGenerator() {
        currentBag = new ArrayList<>();
        fillNewBag();
    }

    public int getNextPiece() {
        if (currentBag.isEmpty()) {
            fillNewBag();
        }
        return currentBag.remove(0);
    }

    private void fillNewBag() {
        currentBag.clear();
        // Add pieces 0-6 in order
        for (int i = 0; i < BAG_SIZE; i++) {
            currentBag.add(i);
        }
        Collections.shuffle(currentBag);
    }
}