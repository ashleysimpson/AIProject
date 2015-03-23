// first thing i did was find the best move and always choose that for only one level
// second i added the checking for multiple levels on the same move selection

package omweso;
//TODO this needs FIXXXXXIIIINNNNGGGGGGGG

import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;
import boardgame.Player;

import java.util.ArrayList;
import java.util.Random;

/** A random Omweso player. */
public class s260415297Player extends Player {
    Random rand = new Random();

    /** You must provide a default public constructor like this,
     * which does nothing but call the base-class constructor with
     * your student number. */
    public s260415297Player() { super("260415297"); }
    public s260415297Player(String s) { super(s); }

    public Board createBoard() { return new CCBoard(); }

    /** Use this method to take actions when the game is over. */
    public void gameOver( String msg, BoardState bs) {
        CCBoardState board_state = (CCBoardState) bs;

        if(board_state.haveWon()){
            System.out.println("I won!");
        }else if(board_state.haveLost()){
            System.out.println("I lost!");
        }else if(board_state.tieGame()){
            System.out.println("Draw!");
        }else{
            System.out.println("Undecided!");
        }
    }

    /** Implement a very stupid way of picking moves. */
    public Move chooseMove(BoardState bs)
    {
        // Cast the arguments to the objects we want to work with.
        CCBoardState board_state = (CCBoardState) bs;

        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits = board_state.getBoard();

        // Our pits in first row of array, opponent pits in second row.
        int[] my_pits = pits[0];
        int[] op_pits = pits[1];

        if(!board_state.isInitialized()){
            // Code for setting up our initial position. Also, if your agent needs to be
            // set-up in any way (e.g. loading data from files) you should do it here.

            //CCBoardState.SIZE is the width of the board, in pits.
            int[] initial_pits = new int[2 * CCBoardState.SIZE];

            // Make sure your initialization move includes the proper number of seeds.
            int num_seeds = CCBoardState.NUM_INITIAL_SEEDS;

            if(board_state.playFirst()){
                // If we're going to play first in this game, choose one random way of setting up.
                // Throw each seed in a random pit that we control.
                for(int i = 0; i < num_seeds; i++){
                    int pit = rand.nextInt(2 * CCBoardState.SIZE);
                    initial_pits[pit]++;
                }
            }else{
                // If we're going to play second this game, choose another random way of setting up.
                // Throw each seed in a random pit that we control, but in the row closest to us.
                for(int i = 0; i < num_seeds; i++){
                    int pit = rand.nextInt(CCBoardState.SIZE);
                    initial_pits[pit]++;
                }
            }

            return new CCMove(initial_pits);
        }else{
            // Play a normal turn.
            ArrayList<CCMove> moves = board_state.getLegalMoves();       
            
            int[][] board = board_state.getBoard();
            int choice = bestMove(board);
            return moves.get(choice);

        }
    }
    
    // simple method to find the best move for only one seed sow
    private int bestMove(int[][] board) {
    	
    	int mostSeedsTaken = 0;
    	int mostSeedsMoved = 0;
    	int bestMoveTaken = 0;
    	int bestMoveMoved = 0;
    	int j = 0;
    	
    	// go through all seed pits once
    	for (int i = 0; i < board[0].length; i++) {
    		
    		// counter for infinite moves
    		int infiniteCounter = 0;
    		
    		// keep track of the next move location
    		int nextMove = i;
    		
    		// if invalid move then continue
    		if (board[0][i] < 2) {
    			continue;
    		}
    		
    		// create a board that can be updated
    		int[][] updatedBoard;
    		
    		// if no good moves then move the pit that has the most seeds contained
    		if (mostSeedsMoved < board[0][i]) {
    			mostSeedsMoved = board[0][i];
    			bestMoveMoved = j;
    		}
    		
    		// handles the most amount of seeds captured by a play
    		int captureMade = captureAmount(i, board);
    		int previousCapture = captureMade;
    		
    		// if a capture made then need to deal with different update of board
    		updatedBoard = updateBoard(i,board);
    		boolean previousWasACapture = captureMade > 0;
    		
    		// next move will be somewhere new in the board
    		if (!previousWasACapture) {
    			nextMove = (nextMove + board[0][i]) % 16;
    		}
    		
    		// go through the relay and capture loop until the end
    		while (updatedBoard[0][nextMove] > 0) {
    			
    			// play the move and update the board
    			captureMade = captureMade + captureAmount(nextMove, updatedBoard);
    			previousCapture = captureMade - previousCapture;
    			
    			// check for next move
    			previousWasACapture = previousCapture > 0;
    			if (!previousWasACapture) {
    				nextMove = (nextMove + updatedBoard[0][nextMove]) % 16;
    			}
    			
    			// update the board after all work computed
    			updatedBoard = updateBoard(nextMove, updatedBoard);

    			// set previous for next round
    			previousCapture = captureMade;
    				
    			infiniteCounter++;
    		}
    		
    		// make sure to make the best move
    		if (mostSeedsTaken < captureMade) {
    			mostSeedsTaken = captureMade;
    			bestMoveTaken = j;
    		}
    		
    		// keeps track of the valid moves
    		j=j+1;
    	}
    	
    	if (mostSeedsTaken > 0) {
    		return bestMoveTaken;
    	} else {
    		return bestMoveMoved;
    	}
    }
    
    // updates the board based on a move made that captures
    private int[][] updateBoard(int selection, int[][] oldBoard) {
    	
    	// create a new board to return
    	int[][] board = new int[2][16];
    	for (int i = 0; i < board.length; i++) {
    		for (int j = 0; j < board[i].length; j++) {
    			board[i][j] = oldBoard[i][j];
    		} 		
    	}
    	// calculate the last pit
    	int lastPit = (selection + board[0][selection]) % 16;
    	
    	// remove the pits from the selection pit
    	board[0][selection] = 0;
    	
    	// fill in pieces that get added as the move is taken
    	for (int i = 1; i < oldBoard[0][selection]; i++) {
    		board[0][(i+selection) % 16] = board[0][(i+selection) % 16] + 1;
    	}
    	
    	// check for each condition where capture occurs and update the board
    	// also update board where capture doesn't occur
		if (lastPit == 15) {
			if (board[0][15] > 0 && board[1][8] > 0 && board[1][7] > 0) {
				board[0][selection] = board[1][8] + board[1][7];
				board[0][15] = board[0][15] + 1;
				board[1][8] = 0;
				board[1][7] = 0;
			} else {
				board[0][15] = board[0][15] + 1;
			}
		} else if (lastPit == 14) {
			if (board[0][14] > 0 && board[1][6] > 0 && board[1][9] > 0) {
				board[0][selection] = board[1][6] + board[1][9];
				board[0][14] = board[0][14] + 1;
				board[1][6] = 0;
				board[1][9] = 0;
			} else {
				board[0][14] = board[0][14] + 1;
			}
		} else if (lastPit == 13) {
			if (board[0][13] > 0 && board[1][5] > 0 && board[1][10] > 0) {
				board[0][selection] = board[1][5] + board[1][10];
				board[0][13] = board[0][13] + 1;
				board[1][5] = 0;
				board[1][10] = 0;
			} else {
				board[0][13] = board[0][13] + 1;
			}
		} else if (lastPit == 12) {
			if (board[0][12] > 0 && board[1][4] > 0 && board[1][11] > 0) {
				board[0][selection] = board[1][4] + board[1][11];
				board[0][12] = board[0][12] + 1;
				board[1][4] = 0;
				board[1][11] = 0;
			} else {
				board[0][12] = board[0][12] + 1;
			}
		} else if (lastPit == 11) {
			if (board[0][11] > 0 && board[1][3] > 0 && board[1][12] > 0) {
				board[0][selection] = board[1][3] + board[1][12];
				board[0][11] = board[0][11] + 1;
				board[1][3] = 0;
				board[1][12] = 0;
			} else {
				board[0][11] = board[0][11] + 1;
			}
		} else if (lastPit == 10) {
			if (board[0][10] > 0 && board[1][2] > 0 && board[1][13] > 0) {
				board[0][selection] = board[1][2] + board[1][13];
				board[0][10] = board[0][10] + 1;
				board[1][2] = 0;
				board[1][13] = 0;
			} else {
				board[0][10] = board[0][10] + 1;
			}
		} else if (lastPit == 9) {
			if (board[0][9] > 0 && board[1][1] > 0 && board[1][14] > 0) {
				board[0][selection] = board[1][1] + board[1][14];
				board[0][9] = board[0][9] + 1;
				board[1][1] = 0;
				board[1][14] = 0;
			} else {
				board[0][9] = board[0][9] + 1;
			}
		} else if (lastPit == 8) {
			if (board[0][8] > 0 && board[1][0] > 0 && board[1][15] > 0) {
				board[0][selection] = board[1][0] + board[1][15];
				board[0][8] = board[0][8] + 1;
				board[1][0] = 0;
				board[1][15] = 0;
			} else {
				board[0][8] = board[0][8] + 1;
			}
		} else if (lastPit == 7) {
			board[0][7] = board[0][7] + 1;
		} else if (lastPit == 6) {
			board[0][6] = board[0][6] + 1;
		} else if (lastPit == 5) {
			board[0][5] = board[0][5] + 1;
		} else if (lastPit == 4) {
			board[0][4] = board[0][4] + 1;
		} else if (lastPit == 3) {
			board[0][3] = board[0][3] + 1;
		} else if (lastPit == 2) {
			board[0][2] = board[0][2] + 1;
		} else if (lastPit == 1) {
			board[0][1] = board[0][1] + 1;
		} else {
			board[0][0] = board[0][0] + 1;
		}
		
		return board;
    }
    
    // returns the amount of captured seeds from this pit (if any)
    private int captureAmount(int selection, int[][] board) {
    	
    	int captured = 0;
    	int lastPit = (selection + board[0][selection]) % 16;
    	
    	// check for each condition where capture occurs
		if (lastPit == 15) {
			if (board[0][15] > 0 && board[1][8] > 0 && board[1][7] > 0) {
				captured = board[1][8] + board[1][7];
			}
		} else if (lastPit == 14) {
			if (board[0][14] > 0 && board[1][6] > 0 && board[1][9] > 0) {
				captured = board[1][6] + board[1][9];
			}
		} else if (lastPit == 13) {
			if (board[0][13] > 0 && board[1][5] > 0 && board[1][10] > 0) {
				captured = board[1][5] + board[1][10];
			}
		} else if (lastPit == 12) {
			if (board[0][12] > 0 && board[1][4] > 0 && board[1][11] > 0) {
				captured = board[1][4] + board[1][11];
			}
		} else if (lastPit == 11) {
			if (board[0][11] > 0 && board[1][3] > 0 && board[1][12] > 0) {
				captured = board[1][3] + board[1][12];
			}
		} else if (lastPit == 10) {
			if (board[0][10] > 0 && board[1][2] > 0 && board[1][13] > 0) {
				captured = board[1][2] + board[1][13];
			}
		} else if (lastPit == 9) {
			if (board[0][9] > 0 && board[1][1] > 0 && board[1][14] > 0) {
				captured = board[1][1] + board[1][14];
			}
		} else if (lastPit == 8) {
			if (board[0][8] > 0 && board[1][0] > 0 && board[1][15] > 0) {
				captured = board[1][0] + board[1][15];
			}
		} else {
			captured = 0;
		}
		
    	return captured;
    }
}
