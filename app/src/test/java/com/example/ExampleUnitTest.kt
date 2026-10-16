package com.example

import com.example.data.model.ImageAdjustments
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultAdjustments_areZero() {
        val adj = ImageAdjustments()
        assertEquals(0f, adj.brightness, 0.001f)
        assertEquals(0f, adj.contrast, 0.001f)
        assertEquals(0f, adj.saturation, 0.001f)
        assertEquals("Normal", adj.filterPreset)
        assertEquals(1.0f, adj.filterIntensity, 0.001f)
    }

    @Test
    fun projectEntity_creation() {
        val project = ProjectEntity(
            id = "proj-123",
            name = "Cyberpunk Shoot",
            type = ProjectType.PHOTO.name,
            lastModified = 1700000000L
        )
        assertNotNull(project)
        assertEquals("Cyberpunk Shoot", project.name)
        assertEquals("PHOTO", project.type)
    }
}
