package roottemplate.calculator.evaluator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class PriorityManager implements Iterable<PriorityManager.PriorityStorage> {
    private final LinkedList<PriorityStorage> priorities = new LinkedList<>();
    
    private ListIterator<PriorityStorage> getListItrByFriendlyName(String name) {
        if(name == null) return null;
        ListIterator<PriorityStorage> it;
        for(it = priorities.listIterator(); it.hasNext();)
            if(it.next().friendlyName.equals(name)) return it;
        return null;
    }
    private void updatePrioritiesStorage(ListIterator<PriorityStorage> it, int delta) {
        for(; it.hasNext(); )
            it.next().priority += delta;
    }
    private PriorityStorage onCreateAlreadyDefined(ListIterator<PriorityStorage> it, String name, boolean leftDirection) {
        PriorityStorage elem = it.previous();
        if(elem.leftDirection != leftDirection)
            throw new IllegalArgumentException("Priority with name \"" + name + "\" already defined with wrong leftDirection");
        elem.usingCount++;
        return elem;
    }
    private void add(ListIterator<PriorityStorage> it, PriorityStorage storage) {
        it.add(storage);
        if(it.hasNext()) {
            if(it.next() == null) it.remove();
            else it.previous();
        } 
    }
    
    public PriorityStorage createPriority(int priority, boolean isLeftDirection) {
        return PriorityManager.this.createPriority(null, priority, isLeftDirection);
    }
    public PriorityStorage createPriority(String friendlyName, boolean isLeftDirection) {
        return PriorityManager.this.createPriority(friendlyName, priorities.size(), isLeftDirection);
    }
    public PriorityStorage createPriority(String friendlyName, int priority, boolean isLeftDirection) {
        if(priority < 0) throw new IllegalArgumentException("Negative priority: " + priority);
        ListIterator<PriorityStorage> it = getListItrByFriendlyName(friendlyName);
        if(it != null) return onCreateAlreadyDefined(it, friendlyName, isLeftDirection);
        
        while(priorities.size() < priority) priorities.add(null);
        it = priorities.listIterator(priority);
        PriorityStorage elem = new PriorityStorage(priority, friendlyName, isLeftDirection);
        add(it, elem);
        updatePrioritiesStorage(it, 1);
        return elem;
    }
    public PriorityStorage createPriority(String after, String friendlyName, boolean isLeftDirection) {
        ListIterator<PriorityStorage> it = getListItrByFriendlyName(friendlyName);
        if(it != null) return onCreateAlreadyDefined(it, friendlyName, isLeftDirection);
        it = getListItrByFriendlyName(after);
        if(it == null) it = priorities.listIterator(priorities.size());
        
        PriorityStorage elem = new PriorityStorage(it.previousIndex(), friendlyName, isLeftDirection);
        add(it, elem);
        updatePrioritiesStorage(it, 1);
        return elem;
    }
    
    public PriorityStorage getPriorityStorageByFriendlyName(String name) {
        ListIterator<PriorityStorage> it = getListItrByFriendlyName(name);
        if(it == null) return null;
        return it.previous();
    }
    public PriorityStorage getPriorityStorageByPriority(int priority) {
        return (priorities.size() > priority) ? priorities.get(priority) : null;
    }
    
    private boolean removeIfNeeded(ListIterator<PriorityStorage> it) {
        if(it == null) return false;
        PriorityStorage elem = it.next();
        if(--elem.usingCount <= 0) {
            it.remove();
            updatePrioritiesStorage(it, -1);
            return true;
        }
        return false;
    }
    public boolean removeIfNeeded(int priority) {
        if(priority >= priorities.size()) return false;
        return removeIfNeeded(priorities.listIterator(priority));
    }
    public boolean removeIfNeeded(String friendlyName) { return removeIfNeeded(getListItrByFriendlyName(friendlyName)); }

    @Override
    public Iterator<PriorityStorage> iterator() { return listIterator(0); }
    public ListIterator<PriorityStorage> listIterator(final int index) {
        return new ListIterator<PriorityStorage>() {
            private final ListIterator<PriorityStorage> it = priorities.listIterator(index);
            
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public PriorityStorage next() { return it.next(); }
            @Override public void remove() { throw new UnsupportedOperationException(); }
            @Override public boolean hasPrevious() { return it.hasPrevious(); }
            @Override public PriorityStorage previous() { return it.previous(); }
            @Override public int nextIndex() { return it.nextIndex(); }
            @Override public int previousIndex() { return it.previousIndex(); }
            @Override public void set(PriorityStorage e) { throw new UnsupportedOperationException(); }
            @Override public void add(PriorityStorage e) { throw new UnsupportedOperationException(); }
        };
    }
    
    public static class PriorityStorage {
        private int priority;
        public final String friendlyName;
        public final boolean leftDirection; // true if left to right direction
        private int usingCount = 1;
        
        
        private PriorityStorage(int priority, String friendlyName, boolean leftDirection) {
            this.priority = priority;
            this.friendlyName = friendlyName;
            this.leftDirection = leftDirection;
        }
        
        public int getPriority() { return priority; }
        public int getUsingCount() { return usingCount; }
        
        public boolean willBeExecutedBefore(PriorityStorage storage, boolean before) {
            if(priority != storage.priority) return priority > storage.priority;
            return leftDirection ^ before ? storage.priority > priority : storage.priority >= priority;
        }
    }
}
