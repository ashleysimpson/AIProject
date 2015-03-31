// first thing i did was find the best move and always choose that for only one level
// second i added the checking for multiple levels on the same move selection
// third implement minimax
// fourth implement alphabeta
// fifth i started with a specific move that ends with a high chance of winning
// sixth i updated my heuristic

package omweso;
//TODO this needs FIXXXXXIIIINNNNGGGGGGGG

import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;
import boardgame.Player;

import java.util.ArrayList;
import java.util.Random;

/** A random Omweso player. */
public class CCRandomPlayer extends Player {
    Random rand = new Random();

    /** You must provide a default public constructor like this,
     * which does nothing but call the base-class constructor with
     * your student number. */
    public CCRandomPlayer() { super("random"); }
    public CCRandomPlayer(String s) { super(s); }

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
                // specific setup
            	initial_pits[8] = 7;
            	initial_pits[9] = 4;
            	initial_pits[10] = 3;
            	initial_pits[11] = 4;
            	initial_pits[12] = 3;
            	initial_pits[13] = 2;
            	initial_pits[14] = 9;
            }else{
            	// specific setup
            	initial_pits[8] = 7;
            	initial_pits[9] = 4;
            	initial_pits[10] = 3;
            	initial_pits[11] = 4;
            	initial_pits[12] = 3;
            	initial_pits[13] = 2;
            	initial_pits[14] = 9;
            }

            return new CCMove(initial_pits);
        }else{
            // Play a normal turn
            ArrayList<CCMove> moves = board_state.getLegalMoves();
            
            int[][] board = board_state.getBoard();
            int choice = bestMove(board);
            return moves.get(choice);

        }
    }
    
    // simple method to find the best move for only one seed sow
    private int bestMove(int[][] board) {
    	
    	int depth = 30;
    	
    	// create the new monte carlo search tree
    	Node tree = new Node(true, 0, board);
    	
    	// setup for searching
    	int j = 0;
    	
       	// go through all seed pits once
    	for (int i = 0; i < board[0].length; i++) {
    		
    		// trackers to be used
    		int cycleTracker = 0;
    		int nextMove = 0;
    		
    		// if invalid move then continue
    		if (board[0][i] < 2) {
    			continue;
    		}
    		
    		// create a board that can be updated
    		int[][] updatedBoard;
    		
    		// handles the most amount of seeds captured by a play
    		int captureMade = captureAmount(i, board, true);
    		int previousCapture = captureMade;
    		int prevMove = 0;
    		
    		// if a capture made then need to deal with different update of board
    		updatedBoard = updateBoard(i,board, true);
    		boolean previousWasACapture = captureMade > 0;
    		
    		// next move will be somewhere new in the board
    		if (!previousWasACapture) {
    			nextMove = (nextMove + board[0][i]) % 16;
    		}
    		
    		// go through the relay and capture loop until the end
    		while (updatedBoard[0][nextMove] > 1 && cycleTracker < 200) {
    			
    			// play the move and update the board
    			captureMade = captureMade + captureAmount(nextMove, updatedBoard, true);
    			previousCapture = captureMade - previousCapture;
    			prevMove = nextMove;
    			
    			// check for next move
    			previousWasACapture = previousCapture > 0;
    			if (!previousWasACapture) {
    				nextMove = (prevMove + updatedBoard[0][prevMove]) % 16;
    			}
    			
    			// update the board after all work computed
    			updatedBoard = updateBoard(prevMove, updatedBoard, true);

    			// set previous for next round
    			previousCapture = captureMade;
    			
    			// check for infinite issue
    			cycleTracker++;
    		}
    		
    		// check winning condition
    		boolean winCheck = true;
    		for (int a = 0; a < updatedBoard[0].length; a++) {
    			if (updatedBoard[0][a] > 1) {
    				winCheck = false;
    				break;
    			}
    		}
    		if (winCheck) {
    			return j;
    		}
    		
    		// randomly simulate from this point
    		int rating = rollout(updatedBoard, false, depth);
    		Node child = new Node(false, rating, updatedBoard);
    		child.setParent(tree);
    		child.setMove(j);
    		tree.addChild(child);
    		
    		// keeps track of the valid moves
    		j=j+1;
    	}
    	
    	// check the running time
    	long start = System.currentTimeMillis();
    	long end = System.currentTimeMillis();
    	long runningTime = end - start;
    	boolean maxPlayer = false;
    	
    	// while we still have time keep checking and adding nodes
    	while (runningTime < 1800) {
    		int[][] updatedBoard;
    		
    		// get the minimax node choice and the updatedBoard
    		Node base = miniMaxNode(tree, maxPlayer);	
    		updatedBoard = base.getBoard().clone();
    		
    		// set the child accordinly
    		int rating = rollout(updatedBoard, maxPlayer, depth);
    		Node child = new Node(false, rating, updatedBoard);	
    		child.setParent(base);
    		base.addChild(child);
    		
    		// set the new values for all parents of child
    		Node fixValues = child;		
    		while (fixValues.getParent() != null) {
    			fixValues = fixValues.getParent();
    			fixValues.setValue(fixValues.getValue() + rating);
    		}
    		
    		// change the max player
    		maxPlayer = !maxPlayer;
    		
    		// reset the time
    		end = System.currentTimeMillis();
    		runningTime = end - start;
    	}
    	
    	return tree.getMaxChild().getMove();
    }
    
    // randomly rollout from the board given
    private int rollout (int[][] board, boolean maxPlayer, int depth) {
    	
    	int p1 = 0;
    	int p2 = 1;
    	if (!maxPlayer) {
    		p1 = 1;
    		p2 = 0;
    	}
    	
    	// check winning condition
		boolean winCheck = true;
		for (int a = 0; a < board[p2].length; a++) {
			if (board[p2][a] > 1) {
				winCheck = false;
				break;
			}
		}
		if (winCheck || depth == 0) {
			if (depth == 0) {
				return 0;
			} else if (maxPlayer){
				return 1;
			} else {
				return -1;
			}
		}
		
		// check losing condition
		boolean lossCheck = true;
		for (int a = 0; a < board[p1].length; a++) {
			if (board[p1][a] > 1) {
				lossCheck = false;
				break;
			}
		}
		if (lossCheck) {
			if (maxPlayer){
				return -1;
			} else {
				return 1;
			}
		}
	
		int mostSeedsTaken = -1000;
		int bestBoard[][] = new int[2][16];
    	
    	// go through all seed pits once
    	for (int i = 0; i < board[p1].length; i++) {
		
    		// keep track of the next move location
    		int nextMove = i;
    		int cycleTracker = 0;
    		
    		// if invalid move then continue
    		if (board[p1][i] < 2) {
    			continue;
    		}
    		
    		// create a board that can be updated
    		int[][] updatedBoard;
    		
    		// handles the most amount of seeds captured by a play
    		int captureMade = captureAmount(i, board, maxPlayer);
    		int previousCapture = captureMade;
    		int prevMove = 0;
    		
    		// if a capture made then need to deal with different update of board
    		updatedBoard = updateBoard(i,board, maxPlayer);
    		boolean previousWasACapture = captureMade > 0;
    		
    		// next move will be somewhere new in the board
    		if (!previousWasACapture) {
    			nextMove = (nextMove + board[p1][i]) % 16;
    		}
    		
    		// go through the relay and capture loop until the end
    		while (updatedBoard[p1][nextMove] > 1 && cycleTracker < 200) {
    			
    			// play the move and update the board
    			captureMade = captureMade + captureAmount(nextMove, updatedBoard, maxPlayer);
    			previousCapture = captureMade - previousCapture;
    			prevMove = nextMove;
    			
    			// check for next move
    			previousWasACapture = previousCapture > 0;
    			if (!previousWasACapture) {
    				nextMove = (prevMove + updatedBoard[p1][prevMove]) % 16;
    			}
    			
    			// update the board after all work computed
    			updatedBoard = updateBoard(prevMove, updatedBoard, maxPlayer);

    			// set previous for next round
    			previousCapture = captureMade;
    			
    			// check for infinite issue
    			cycleTracker++;
    		} 		
  
    		// make sure to make the best move
    		if (mostSeedsTaken < captureMade ) {
    			mostSeedsTaken = captureMade;
    			bestBoard = updatedBoard.clone();
    		}
    	}
    
    	return rollout(bestBoard, !maxPlayer, depth-1);
    }
    
    // minimax to the end of the tree to find where rollout should start
    private Node miniMaxNode (Node tree, boolean maxPlayer) {
    	
    	while (tree.getMaxChild() != null) {
    		if (maxPlayer) {
    			tree = tree.getMaxChild();
    		} else {
    			tree = tree.getMinChild();
    		}
    		maxPlayer = !maxPlayer;
    	}
    	
    	return tree;
    }
    
    // updates the board based on a move made that captures
    private int[][] updateBoard(int selection, int[][] oldBoard, boolean isMaxPlayer) {
    	
    	// setup the board conditions for max and min players
    	int p1 = 0;
    	int p2 = 1;
    	if (!isMaxPlayer) {
    		p1 = 1;
    		p2 = 0;
    	}
    	
    	// create a new board to return
    	int[][] board = new int[2][16];
    	for (int i = 0; i < board.length; i++) {
    		for (int j = 0; j < board[i].length; j++) {
    			board[i][j] = oldBoard[i][j];
    		} 		
    	}
    	// calculate the last pit
    	int lastPit = (selection + board[p1][selection]) % 16;
    	
    	// remove the pits from the selection pit
    	board[p1][selection] = 0;
    	
    	// fill in pieces that get added as the move is taken
    	for (int i = 1; i < oldBoard[p1][selection]; i++) {
    		board[p1][(i+selection) % 16] = board[p1][(i+selection) % 16] + 1;
    	}
    	
    	// check for each condition where capture occurs and update the board
    	// also update board where capture doesn't occur
		if (lastPit == 15) {
			if (board[p1][15] > 0 && board[p2][8] > 0 && board[p2][7] > 0) {
				board[p1][selection] = board[p2][8] + board[p2][7];
				board[p1][15] = board[p1][15] + 1;
				board[p2][8] = 0;
				board[p2][7] = 0;
			} else {
				board[0][15] = board[0][15] + 1;
			}
		} else if (lastPit == 14) {
			if (board[p1][14] > 0 && board[p2][6] > 0 && board[p2][9] > 0) {
				board[p1][selection] = board[p2][6] + board[p2][9];
				board[p1][14] = board[p1][14] + 1;
				board[p2][6] = 0;
				board[p2][9] = 0;
			} else {
				board[0][14] = board[0][14] + 1;
			}
		} else if (lastPit == 13) {
			if (board[p1][13] > 0 && board[p2][5] > 0 && board[p2][10] > 0) {
				board[p1][selection] = board[p2][5] + board[p2][10];
				board[p1][13] = board[p1][13] + 1;
				board[p2][5] = 0;
				board[p2][10] = 0;
			} else {
				board[0][13] = board[0][13] + 1;
			}
		} else if (lastPit == 12) {
			if (board[p1][12] > 0 && board[p2][4] > 0 && board[p2][11] > 0) {
				board[p1][selection] = board[p2][4] + board[p2][11];
				board[p1][12] = board[p1][12] + 1;
				board[p2][4] = 0;
				board[p2][11] = 0;
			} else {
				board[0][12] = board[0][12] + 1;
			}
		} else if (lastPit == 11) {
			if (board[p1][11] > 0 && board[p2][3] > 0 && board[p2][12] > 0) {
				board[p1][selection] = board[p2][3] + board[p2][12];
				board[p1][11] = board[p1][11] + 1;
				board[p2][3] = 0;
				board[p2][12] = 0;
			} else {
				board[0][11] = board[0][11] + 1;
			}
		} else if (lastPit == 10) {
			if (board[p1][10] > 0 && board[p2][2] > 0 && board[p2][13] > 0) {
				board[p1][selection] = board[p2][2] + board[p2][13];
				board[p1][10] = board[p1][10] + 1;
				board[p2][2] = 0;
				board[p2][13] = 0;
			} else {
				board[0][10] = board[0][10] + 1;
			}
		} else if (lastPit == 9) {
			if (board[p1][9] > 0 && board[p2][1] > 0 && board[p2][14] > 0) {
				board[p1][selection] = board[p2][1] + board[p2][14];
				board[p1][9] = board[p1][9] + 1;
				board[p2][1] = 0;
				board[p2][14] = 0;
			} else {
				board[0][9] = board[0][9] + 1;
			}
		} else if (lastPit == 8) {
			if (board[p1][8] > 0 && board[p2][0] > 0 && board[p2][15] > 0) {
				board[p1][selection] = board[p2][0] + board[p2][15];
				board[p1][8] = board[p1][8] + 1;
				board[p2][0] = 0;
				board[p2][15] = 0;
			} else {
				board[0][8] = board[0][8] + 1;
			}
		} else if (lastPit == 7) {
			board[p1][7] = board[p1][7] + 1;
		} else if (lastPit == 6) {
			board[p1][6] = board[p1][6] + 1;
		} else if (lastPit == 5) {
			board[p1][5] = board[p1][5] + 1;
		} else if (lastPit == 4) {
			board[p1][4] = board[p1][4] + 1;
		} else if (lastPit == 3) {
			board[p1][3] = board[p1][3] + 1;
		} else if (lastPit == 2) {
			board[p1][2] = board[p1][2] + 1;
		} else if (lastPit == 1) {
			board[p1][1] = board[p1][1] + 1;
		} else {
			board[p1][0] = board[p1][0] + 1;
		}
		
		return board;
    }
    
    // returns the amount of captured seeds from this pit (if any)
    private int captureAmount(int selection, int[][] board, boolean isMaxPlayer) {
    	
    	// setup the board conditions for max and min players
    	int p1 = 0;
    	int p2 = 1;
    	if (!isMaxPlayer) {
    		p1 = 1;
    		p2 = 0;
    	}
    	
    	int captured = 0;
    	int lastPit = (selection + board[p1][selection]) % 16;
    	
    	// check for each condition where capture occurs
		if (lastPit == 15) {
			if (board[p1][15] > 0 && board[p2][8] > 0 && board[p2][7] > 0) {
				captured = board[p2][8] + board[p2][7];
			}
		} else if (lastPit == 14) {
			if (board[p1][14] > 0 && board[p2][6] > 0 && board[p2][9] > 0) {
				captured = board[p2][6] + board[p2][9];
			}
		} else if (lastPit == 13) {
			if (board[p1][13] > 0 && board[p2][5] > 0 && board[p2][10] > 0) {
				captured = board[p2][5] + board[p2][10];
			}
		} else if (lastPit == 12) {
			if (board[p1][12] > 0 && board[p2][4] > 0 && board[p2][11] > 0) {
				captured = board[p2][4] + board[p2][11];
			}
		} else if (lastPit == 11) {
			if (board[p1][11] > 0 && board[p2][3] > 0 && board[p2][12] > 0) {
				captured = board[p2][3] + board[p2][12];
			}
		} else if (lastPit == 10) {
			if (board[p1][10] > 0 && board[p2][2] > 0 && board[p2][13] > 0) {
				captured = board[p2][2] + board[p2][13];
			}
		} else if (lastPit == 9) {
			if (board[p1][9] > 0 && board[p2][1] > 0 && board[p2][14] > 0) {
				captured = board[p2][1] + board[p2][14];
			}
		} else if (lastPit == 8) {
			if (board[p1][8] > 0 && board[p2][0] > 0 && board[p2][15] > 0) {
				captured = board[p2][0] + board[p2][15];
			}
		} else {
			captured = 0;
		}
		
    	return captured;
    }
    
    // Node class used for monte carlo search
    public class Node {
    	private ArrayList<Node> children = new ArrayList<Node>();
    	private Node parent;
    	boolean isRoot;
    	private int upperValue;
    	private int[][] boardConfig;
    	private int move;
    	
    	public Node(boolean isNewTree, int upperValue, int[][] board) {
    		if (isNewTree) {
    			isRoot = true;
    			parent = null;
    		} else {
    			isRoot = false;
    		}
    		this.upperValue = upperValue;
			this.boardConfig = board.clone();
			this.move = 0;
    	}
    	
    	public void addChild (Node child) {
    		children.add(child);
    	}
    	
    	public Node getParent () {
    		return parent;
    	}
    	
    	public int getValue()  {
    		return upperValue;
    	}
    	
    	public void setValue (int upper) {
    		upperValue = upper;
    	}   
    	
    	public int getMove() {
    		return move;
    	}
    	
    	public void setMove(int j) {
    		this.move = j;
    	}
    	
    	public void setParent (Node parent) {
    		this.parent = parent;
    	}
    	
    	public int[][] getBoard() {
    		return boardConfig;
    	}
    	
    	public Node getMinChild() {
    		int min = 1000000;
    		Node minNode = null;
    		for (int i = 0; i < children.size(); i++) {
    			if (children.get(i).getValue() < min) {
    				min = children.get(i).getValue();
    				minNode = children.get(i);
    			}
    		}
    		return minNode;
    	}
    	
    	public Node getMaxChild() {
    		int max = -1000000;
    		Node maxNode = null;
    		for (int i = 0; i < children.size(); i++) {
    			if (children.get(i).getValue() > max) {
    				max = children.get(i).getValue();
    				maxNode = children.get(i);
    			}
    		}
    		return maxNode;
    	}
    	
    	public Node getIChild(int i) {
    		return children.get(i);
    	}
    }
}
