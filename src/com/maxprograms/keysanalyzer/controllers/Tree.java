package com.maxprograms.keysanalyzer.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Tree<T> implements Serializable {
  
	private static final long serialVersionUID = 5458283808659252781L;
	private Node<T> root;

    public Tree(T rootData) {
        root = new Node<T>(rootData);
    }

    public Tree(Node<T> node) {
        root = node;
    }

    public Node<T> getRoot() {
		return root;
	}
    
	public static class Node<T> {
        private T data;
        private Node<T> parent;
        private ArrayList<Node<T>> children;
        
        public Node(T value) {
        	data = value;
        	children = new ArrayList<Node<T>>();
        }
        
        public void addChild(Node<T> node) {
        	node.parent = this;
        	children.add(node);
        }
        
        public void removeChild(Node<T> node) {
        	children.remove(node);
        }
        
        public Node<T> getChild(T value) {
        	Iterator<Node<T>> it = children.iterator();
        	while (it.hasNext()) {
        		Node<T> child = it.next();
        		if (value.equals(child.data)) {
        			return child;
        		}
        	}
        	return null;
        }
        
        public Iterator<Node<T>> iterator() {
        	return children.iterator();
        }
        
        public Node<T> getParent() {
        	return parent;
        }

		public T getData() {
			return data;
		}		
		
		public int size() {
			return children.size();
		}

		public Node<T> getChild(int i) {
			return children.get(i);
		}
    }

	public Tree<T> prune() {
		Node<T> newRoot = root;
		while (newRoot.size() == 1) {
			newRoot = newRoot.getChild(0);
		}
		return new Tree<T>(newRoot);
	}
}