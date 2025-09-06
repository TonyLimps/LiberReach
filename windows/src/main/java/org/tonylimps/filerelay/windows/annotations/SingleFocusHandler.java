package org.tonylimps.filerelay.windows.annotations;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SingleFocusHandler {

	private HashMap<String,List<Node>> focusedLists;

	public void handle(Object... controllers) {
		focusedLists = new HashMap<>();
		Arrays.stream(controllers).forEach(controller -> {
			Class<?> controllerClass = controller.getClass();
			Arrays.stream(controllerClass.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(SingleFocus.class) && Node.class.isAssignableFrom(field.getType()))
				.forEach(field -> {
					try{
						field.setAccessible(true);
						Node control = (Node)field.get(controller);
						SingleFocus annotation = field.getAnnotation(SingleFocus.class);
						setFocused(control, annotation.focused());
						if(!focusedLists.containsKey(annotation.group())){
							focusedLists.put(annotation.group(), new ArrayList<>());
						}
						control.focusedProperty().addListener((observable, oldValue, newValue) -> {
							if(!oldValue && newValue){
								focusedLists.get(annotation.group()).stream()
									.filter(node -> node!=control)
									.forEach(node -> setFocused(node, false));
							}
						});
						focusedLists.get(annotation.group()).add(control);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
		});
	}

	private void setFocused(Node node, boolean focused) {
		Platform.runLater(() -> {
			if (focused) {
				node.requestFocus();
			}
			else {
				if(node instanceof ListView){
					((ListView<?>) node).getSelectionModel().clearSelection();
				}
				node.setFocusTraversable(false);
				node.setFocusTraversable(true);
			}
		});
	}
}
