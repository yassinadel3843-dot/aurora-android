package com.aurora.workspace

import com.aurora.workspace.data.Project
import com.aurora.workspace.data.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    private fun project(vararg tasks: TaskItem): Project = Project(
        id = "p1",
        name = "Test project",
        description = "",
        emoji = "\u2728",
        accent = 0,
        dueLabel = "Soon",
        memberIds = mutableListOf("me"),
        tasks = tasks.toMutableList()
    )

    @Test
    fun emptyProject_hasZeroProgress_andIsNotDone() {
        val p = project()
        assertEquals(0, p.progress)
        assertFalse(p.isDone)
        assertEquals(0, p.doneCount)
    }

    @Test
    fun allTasksDone_means100Percent_andIsDone() {
        val p = project(TaskItem("t1", "A", true), TaskItem("t2", "B", true))
        assertEquals(100, p.progress)
        assertTrue(p.isDone)
        assertEquals(2, p.doneCount)
    }

    @Test
    fun halfDone_roundsTo50Percent_andIsNotDone() {
        val p = project(TaskItem("t1", "A", true), TaskItem("t2", "B", false))
        assertEquals(50, p.progress)
        assertFalse(p.isDone)
        assertEquals(1, p.doneCount)
    }

    @Test
    fun threeTasks_oneDone_roundsTo33Percent() {
        val p = project(
            TaskItem("t1", "A", true),
            TaskItem("t2", "B", false),
            TaskItem("t3", "C", false)
        )
        assertEquals(33, p.progress)
    }

    @Test
    fun taskPriority_defaultsToNone_andTagToEmpty() {
        val task = TaskItem("t1", "A", false)
        assertEquals("NONE", task.priority)
        assertEquals("", task.tag)
    }

    @Test
    fun taskPriority_canBeSetAndReadsBack() {
        val task = TaskItem("t1", "A", false, priority = "HIGH", tag = "launch")
        assertEquals("HIGH", task.priority)
        assertEquals("launch", task.tag)
    }
}
