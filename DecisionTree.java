/*Machine Learning Homework : Decision Tree
Vidya Sri Mani
vxm163230
*/

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/*The following program constructs a decision tree using the training data, 
prunes it using testing data and classifies using validation data
*/

//Every node in the tree can be considered an object of the decision tree
class DecTree {
	int label; //either 0 or 1
	DecTree left;
	DecTree right;
	DecTree parent;
	int leafnode; //1 denotes if it is a leaf node
	int attribute; //attributes of the node
	int leftData[];
	int rightData[];
	
	DecTree(){
		label = -1;
		attribute = -1;
	}
}


public class DecisionTree{
	
	private static int rows = 0;//number of rows in the data set, including the labels
	private static int columns = 0;//number of attributes + classification
	private static int count = 0;
	
	public static void main(String[] args) {
		//int L = 0;
		int L = Integer.parseInt(args[0]);
		//int K = 1;
		int K = Integer.parseInt(args[1]);
		 //String filename = "C:\\Users\\Vidya\\workspace\\DecisionTree\\src\\training_set.csv";
		 String filename = args[2];//args[2] refers to the filename
		 int[][] values = createDataSet(filename);
		 String[] labels = new String[columns];
		 int[] checked = new int[columns];
		 int[] indexList = new int[values.length];
		 for(int i= 0;i<columns;i++)
			 checked[i] = 0;
		 for(int i = 0;i<values.length;i++)
			 indexList[i] = i;//0,1,2...
		 labels = createLabels(filename);
		 
		 DecTree root = constructDecisionTree(null, values, checked, indexList, null);
		 System.out.println("Before Pruning");
		 printTree(root,0,labels);
		 System.out.println("__________________________________________________________");
		 String testing_file = args[3];//args[3] refers to the filename
		 //String testing_file = "C:\\Users\\Vidya\\workspace\\DecisionTree\\src\\test_set.csv";
		 
		 //System.out.println(("Accuracy:" + Accuracy(filename, root)*100));
		 //Prune Tree
		 
		 DecTree prunedTree = Prune(L, K,testing_file, root, values);
		 printTree(prunedTree,0,labels);
		 System.out.println("__________________________________________________________");
		 //System.out.println("Accuracy:" + (Accuracy(testing_file, prunedTree)*100));
		 
		 DecTree varTree = variance(null, values, checked, indexList,null);
		 printTree(varTree,0,labels);	
		 System.out.println("__________________________________________________________");
		 DecTree varTreePruned = Prune(L, K, testing_file, root, values);
		 printTree(varTreePruned,0,labels);
		 //System.out.println("Accuracy:" + (Accuracy(testing_file, varTreePruned)*100));
        
	}
	
	
	
	private static int[][] createDataSet(String csvFile) {
		BufferedReader Reader = null;
		String line = "";
		int count = 0;
		try {

			Reader = new BufferedReader(new FileReader(csvFile));
			while ((line = Reader.readLine()) != null) {
				if (count == 0) {
					String[] label = line.split(",");
					columns = label.length;
				}
				count++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (Reader != null) {
				try {
					Reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		rows = count;
		int[][] dataset = new int[rows-1][columns];//600x21
		BufferedReader Reader2 = null;
		try {

			int i=0,j=0;
			int headingflag = 0;
			Reader2 = new BufferedReader(new FileReader(csvFile));
			while ((line = Reader2.readLine()) != null) {
				headingflag++;
				
					String[] data = line.split(",");
					if(headingflag !=1){
						for(j=0;j<data.length;j++)
						{
							dataset[i][j] = Integer.parseInt(data[j]);
						}
						
						i++;
					}	
				
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (Reader != null) {
				try {
					Reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return dataset;

	}	
	private static String[] createLabels(String csvFile) {
		BufferedReader Reader = null;
		String line = "";
		try {

			Reader = new BufferedReader(new FileReader(csvFile));
			while ((line = Reader.readLine()) != null) {				
					String[] label = line.split(",");
					return label;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (Reader != null) {
				try {
					Reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return line.split(",");
	}

	
	public static DecTree constructDecisionTree(DecTree root, int[][] values, int[] checked, int[] indexList,DecTree parent) {
		if (root == null) {
			root = new DecTree();//Creating a new root node
			if (indexList.length == 0 || indexList == null) {
				root.label = classify(root, values);
				root.leafnode = 1;
				return root;
			}
			if (CheckPositives(indexList, values)) {
				root.label = 1;
				root.leafnode = 1;
				return root;
			}
			if (CheckNegatives(indexList, values)) {
				root.label = 0;//if all values are 0
				root.leafnode = 1;
				return root;
			}
			
			boolean checkedAll = true;
			for (int i=0;i<checked.length;i++) {
				if (checked[i] == 0)
					checkedAll = false;
			}

			if (columns-1 == 1 || checkedAll) {
				root.label = classify(root, values);
				root.leafnode = 1;
				return root;
			}
		}
		root = ConstructNode(root, values, checked, indexList);
		root.parent = parent;
		if (root.attribute != -1)
			checked[root.attribute] = 1;
		int left[] = new int[checked.length];
		int right[] = new int[checked.length];
		for (int j = 0; j < checked.length; j++) {
			left[j] = checked[j];
			right[j] = checked[j];

		}

		root.left = constructDecisionTree(root.left, values, left,root.leftData, root);
		root.right = constructDecisionTree(root.right, values, right, root.rightData, root);
		return root;
	}


	
	public static int classify(DecTree node, int[][] values) {
		
		//Every node is classifies based on the number of positives and the number of negatives
		int positives = 0, negatives =0;
		if (node.parent == null) {
			for (int i = 0; i < values.length; i++) {
				if (values[i][columns-1] == 1) {
					positives++;
				} else {
					negatives++;
				}
			}
		} else {
			for (int i : node.parent.leftData) {
				if (values[i][columns-1] == 1) 
					positives++;
				 else 
					negatives++;
				
			}

			for (int i : node.parent.rightData) {
				if (values[i][columns-1] == 1) 
					positives++;
				else 
					negatives++;
				
			}
		}
		if(positives>negatives)
			return 1;
		else
			return 0;

	}
	public static boolean CheckPositives(int[] indexList, int[][] values) {
		boolean flag = true;
		//even if one negative is present, return false
		for (int i=0 ;i<indexList.length;i++) {
			if (values[i][columns-1] == 0) 
				flag = false;
		}
		return flag;
	}

	
	public static boolean CheckNegatives(int[] indexList, int[][] values) {
		boolean flag = true;
		
		//even if one positive is present, false is returned
		for (int i=0;i< indexList.length;i++) {
			if (values[i][columns-1] == 1) {
				flag = false;
			}
		}
		return flag;

	}
	
		private static double log(double val) {
		return Math.log10(val) / Math.log10(2);
	}
	
	private static void printTree(DecTree root,int delimiter, String[] labels) {
		int space = delimiter;
		if (root.leafnode == 1) {
			System.out.println(" " + root.label);
			return;
		}
		for (int i = 0; i < space; i++) {
			System.out.print("| ");
		}
		if (root.left != null){
			if(root.left.leafnode==1 && root.attribute != -1){
				System.out.print(labels[root.attribute] + "= 0 :");
			}
		}
			
		else if (root.attribute != -1)
			System.out.println(labels[root.attribute] + "= 0 :");

		space++;
		printTree(root.left, space,labels);
		for (int i = 0; i < space; i++) {
			System.out.print("| ");
		}
		if (root.right != null && root.right.leafnode==1 && root.attribute != -1)
			System.out.print(labels[root.attribute] + "= 1 :");
		else if (root.attribute != -1)
			System.out.println(labels[root.attribute] + "= 1 :");
		printTree(root.right, space, labels);
	}
	
	public static DecTree Prune( int L, int K,String file, DecTree root, int[][] values) {
		DecTree tree = new DecTree();
		int i = 0;
		tree = root;
		double maxAccuracy = Accuracy(file, root);
		for (i = 0; i < L; i++) {
			DecTree newRoot = duplicate(root);
			Random randomNumbers = new Random();
			int M = 1 + randomNumbers.nextInt(K);
			for (int j = 1; j <= M; j++) {
				count = 0;
				int noOfNonLeafNodes = CountNonLeafNodes(newRoot);
				if (noOfNonLeafNodes == 0)
					break;
				DecTree nodeArray[] = new DecTree[noOfNonLeafNodes];
				FillArray(newRoot, nodeArray);
				int s = randomNumbers.nextInt(noOfNonLeafNodes);
				nodeArray[s].leafnode = 1;
				nodeArray[s].label = classify(nodeArray[s], values);
				nodeArray[s].left = null;
				nodeArray[s].right = null;

			}
			double accuracy = Accuracy(file, newRoot);

			if (accuracy > maxAccuracy) {
				tree = newRoot;
				maxAccuracy = accuracy;
			}
		}
		return tree;

	}
	
	
	
	private static int CountNonLeafNodes(DecTree root) {
		if (root == null || root.leafnode==1)
			return 0;
		else
			return (1 + CountNonLeafNodes(root.left) + CountNonLeafNodes(root.right));
	}
	
	private static void FillArray(DecTree root, DecTree[] Array) {
		if (root == null || root.leafnode==1) {
			return;
		}
		Array[count++] = root;

		FillArray(root.left, Array);

		FillArray(root.right, Array);

	}
	
	
	public static DecTree duplicate(DecTree node) {
		if (node == null)
			return node;

		DecTree temp = new DecTree();
		temp.label = node.label;
		temp.leafnode = node.leafnode;
		temp.leftData = node.leftData;
		temp.rightData = node.rightData;
		temp.attribute = node.attribute;
		temp.parent = node.parent;
		temp.left = duplicate(node.left);
		temp.right = duplicate(node.right);
		return temp;
	}
	
	private static double Accuracy(String file, DecTree root) {
		int[][] validationSet = new int[rows][columns];
		BufferedReader Reader = null;
		String line = "";
		double returnval = 0;
		double r=rows;
		double c= columns;
		double val = (r-1)/(c*100);
		
		try {
			Reader = new BufferedReader(new FileReader(file));
			int i = 0,j=0;
			int count = 0;
			returnval += val;
			while ((line = Reader.readLine()) != null) {
				String[] lineParameters = line.split(",");
				if (count == 0) {
					count++;
					
					continue;
				} else {
					for (String lineParameter : lineParameters) {
						if(i<rows){
							validationSet[i][j] = Integer.parseInt(lineParameter);
						}
					}
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (Reader != null) {
				try {
					Reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		double x = 0;
		
		for (int i = 1; i < validationSet.length; i++) {
			x = x + clsCheck(validationSet[i], root);
		}
		
		
		returnval += x / ((validationSet.length));
		//System.out.println(x);
		//System.out.println(validationSet.length-1);
		return returnval;
	}
	
	
	private static int clsCheck(int[] val, DecTree node) {
		int index = node.attribute;
		int classified = 1;
		int n = val.length - 1;
		DecTree test = node;
		while (test.label == -1) {
			if (val[index] == 1) {
				test = test.right;
			} else {
				test = test.left;
			}
			
			if (val[n] == test.label) {
					classified = 1;
				}
			else classified = 0;
			index = test.attribute;
		}
		return classified;
	}
	
	private static DecTree variance(DecTree root, int[][] values, int[] checked, int[] indexList,DecTree parent) {
		if (root == null) {
			root = new DecTree();
			if (indexList == null || indexList.length == 0) {
				root.label = classify(root, values);
				root.leafnode = 1;
				return root;
			}
			if (CheckPositives(indexList, values)) {
				root.label = 1;
				root.leafnode = 1;
				return root;
			}
			if (CheckNegatives(indexList, values)) {
				root.label = 0;
				root.leafnode = 1;
				return root;
			}
			boolean checkedAll = true;
			for (int i=0;i<checked.length;i++) {
				if (checked[i] == 0)
					checkedAll = false;
			}

			if (columns-1 == 1 || checkedAll) {
				root.label = classify(root, values);
				root.leafnode = 1;
				return root;
			}

		}
		
		root = VarianceConstruct(root, values, checked, indexList);
		root.parent = parent;
		if (root.attribute != -1)
			checked[root.attribute] = 1;
		int leftchk[] = new int[checked.length];
		int rightchk[] = new int[checked.length];
		for (int j = 0; j < checked.length; j++) {
			leftchk[j] = checked[j];
			rightchk[j] = checked[j];

		}

		root.left = constructDecisionTree(root.left, values, leftchk, root.leftData, root);
		root.right = constructDecisionTree(root.right, values, rightchk, root.rightData, root);
		return root;
	}
	private static DecTree VarianceConstruct(DecTree root, int[][] values, int[] checked, int[] indexList) {
		int i = 0,j=0;
		double maxGain = 0;
		int maxLeftIndex[] = null;
		int maxRightIndex[] = null;
		int maxIndex = -1;
		for (; i < columns-1; i++) {
			if (checked[i] == 0) {
				double negatives = 0;
				double positives = 0;
				double left = 0;
				double right = 0;
				double leftvariance = 0;
				double rightvariance = 0;

				double variance = 0;
				double rightPositives = 0;
				double Gain = 0;
				double rightNegatives = 0, leftPositives = 0, leftNegatives = 0;

				int[] leftIndex = new int[values.length];
				int[] rightIndex = new int[values.length];
				for (j = 0; j < indexList.length; j++) {
					if (values[indexList[j]][columns-1] == 1) {
						positives++;
					} else {
						negatives++;
					}
					if (values[indexList[j]][i] == 1) {
						rightIndex[(int) right++] = indexList[j];
						if (values[indexList[j]][columns-1] == 1) {
							rightPositives++;
						} else {
							rightNegatives++;
						}

					} else {
						leftIndex[(int) left++] = indexList[j];
						if (values[indexList[j]][columns-1] == 1) {
							leftPositives++;
						} else {
							leftNegatives++;
						}

					}

				}
				double p = positives / indexList.length;
				double n = negatives / indexList.length;
				double lp = leftPositives / (leftPositives + leftNegatives);
				double ln = leftNegatives / (leftPositives + leftNegatives);
				double rp = rightPositives / (rightPositives + rightNegatives);
				double rn = rightNegatives / (rightPositives + rightNegatives);
				variance = p*n;
				leftvariance = lp*ln;
				rightvariance = rp*rn;
				if (Double.compare(Double.NaN, variance) == 0) {
					variance = 0;
				}
				if (Double.compare(Double.NaN, leftvariance) == 0) {
					leftvariance = 0;
				}
				if (Double.compare(Double.NaN, rightvariance) == 0) {
					rightvariance = 0;
				}

				Gain = variance - ((left / (left + right) * leftvariance) + (right / (left + right) * rightvariance));

				if (Gain >= maxGain) {
					maxGain = Gain;
					maxIndex = i;
					int leftTempArray[] = new int[(int) left];
					for (int index = 0; index < left; index++) {
						leftTempArray[index] = leftIndex[index];
					}
					int rightTempArray[] = new int[(int) right];
					for (int index = 0; index < right; index++) {
						rightTempArray[index] = rightIndex[index];
					}
					maxLeftIndex = leftTempArray;
					maxRightIndex = rightTempArray;

				}
			}
		}
		root.attribute = maxIndex;
		root.leftData = maxLeftIndex;
		root.rightData = maxRightIndex;
		return root;
	}
	private static DecTree ConstructNode(DecTree node, int[][] values, int[] checked, int[] indexList) 
	{
		int leftarr[] = null;
		int rightarr[] = null;
		int maxIndex = -1;
		double max = 0;
		for (int i=0; i < columns-1; i++) {
			if (checked[i] == 0) {
			//initialize when not checked
				double infoGain = 0;
				double entropy = 0;
				double leftEntropy = 0;
				double rightEntropy = 0;
				
				double negatives = 0;
				double positives = 0;
				
				double left = 0;
				double right = 0;
				
				int[] leftIndex = new int[values.length];
				int[] rightIndex = new int[values.length];
				
				double rightPositives = 0;
				double rightNegatives = 0;
				
				double leftPositives = 0;
				double leftNegatives = 0;
				
				for (int j = 0; j < indexList.length; j++) {
					if (values[indexList[j]][columns-1] == 1) {
						positives++;
					} else {
						negatives++;
					}
					
					if (values[indexList[j]][i] == 1) {
							rightIndex[(int) right++] = indexList[j];
						if (values[indexList[j]][columns-1] == 1) {
							rightPositives++;
						} else {
							rightNegatives++;
						}

					} else {
						leftIndex[(int) left++] = indexList[j];
						if (values[indexList[j]][columns-1] == 1) {
							leftPositives++;
						} else {
							leftNegatives++;
						}

					}

				}

			    double p = positives / indexList.length;
				double n = negatives / indexList.length;
				double lp = leftPositives / (leftPositives + leftNegatives);
				double ln = leftNegatives / (leftPositives + leftNegatives);
				double rp = rightPositives / (rightPositives + rightNegatives);
				double rn = rightNegatives / (rightPositives + rightNegatives);
				//calculate entropy
				entropy = (-1*log(p)*(p)) + (-1*log(n)*(n));
				
				leftEntropy = (-1*log(lp)*(lp))+(-1*log(ln)*(ln));
				rightEntropy = (-1 * log(rp)*(rp))+(-1 * log(rn)*(rn));
				if (Double.compare(Double.NaN, entropy) == 0) {
					entropy = 0;
				}
				if (Double.compare(Double.NaN, leftEntropy) == 0) {
					leftEntropy = 0;
				}
				if (Double.compare(Double.NaN, rightEntropy) == 0) {
					rightEntropy = 0;
				}

				infoGain = entropy- ((left / (left + right) * leftEntropy) + (right / (left + right) * rightEntropy));
				if (infoGain >= max) {
					max = infoGain;
					maxIndex = i;
					int leftTempArray[] = new int[(int) left];
					for (int index = 0; index < left; index++) {
						leftTempArray[index] = leftIndex[index];
					}
					int rightTempArray[] = new int[(int) right];
					for (int index = 0; index < right; index++) {
						rightTempArray[index] = rightIndex[index];
					}
					leftarr = leftTempArray;
					rightarr = rightTempArray;

				}
			}
		}
		node.attribute = maxIndex;
		node.leftData = leftarr;
		node.rightData = rightarr;
		return node;
	}

}