package ru.kulikovman.tasklist;


class TestSingleton {
    private static final TestSingleton ourInstance = new TestSingleton();

    static TestSingleton getInstance() {
        return ourInstance;
    }

    private TestSingleton() {
    }
}
