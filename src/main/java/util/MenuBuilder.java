package util;

import java.util.ArrayList;
import java.util.List;

import static util.ConsoleUtil.println;
import static util.InputHandler.getIntInput;
import static util.LoggerUtil.error;

public class MenuBuilder {
    
    private final String title;
    private final List<MenuItem> menuItems;

    public MenuBuilder(String title) {
        this.title = title;
        this.menuItems = new ArrayList<>();
    }

    public MenuBuilder addMenuItem(String label, Runnable action) {
        menuItems.add(new MenuItem(label, action));
        return this;
    }

    public MenuBuilder addExitItem(String label) {
        menuItems.add(new MenuItem(label, null));
        return this;
    }

    public void show() {
        boolean running = true;
        
        while (running) {
            println("\n=== " + title + " ===");
            
            for (int i = 0; i < menuItems.size(); i++) {
                println((i + 1) + ". " + menuItems.get(i).label());
            }
            
            int choice = getIntInput("Выберите действие: ");
            
            if (choice >= 1 && choice <= menuItems.size()) {
                MenuItem selectedItem = menuItems.get(choice - 1);
                
                if (selectedItem.action() == null) {
                    running = false;
                } else {
                    try {
                        selectedItem.action().run();
                    } catch (Exception e) {
                        error(e.getMessage());
                    }
                }
            } else {
                println("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private record MenuItem(String label, Runnable action) {
    }
}