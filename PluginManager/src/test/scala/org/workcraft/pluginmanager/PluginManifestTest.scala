package org.workcraft.pluginmanager

import org.scalatest.Spec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.UUID

@RunWith(classOf[JUnitRunner])
class PluginManifestTest extends Spec {

  def correct(list: Traversable[String]) = {
    val l = list.toIndexedSeq
    (l.length == 2) && (l(0).equals("org.workcraft.GoodPluginA")) && (l(1).equals("org.workcraft.GoodPluginB"))
  }

  val guid1 = UUID.fromString("b9a4c2f9-d937-4abd-9e50-c9fdb156a28e")

  describe("PluginManifest") {
    it("should report a missing or unreadable file as exception") {
      PluginManifest.read(guid1, "no such file") match {
        case Right(_) => fail("expected error value")
        case Left(error) => error match {
          case ManifestReadError.Exception(_) => {}
          case x => fail("expected Empty, got " + x.getClass().getSimpleName())
        }
      }
    }

    it("should report an empty manifest as error") {
      PluginManifest.read(guid1, ClassLoader.getSystemResource("emptyManifest").getPath()) match {
        case Right(_) => fail("expected error value")
        case Left(error) => error match {
          case ManifestReadError.Empty() => {}
          case x => fail("expected Empty, got " + x.getClass().getSimpleName())
        }
      }
    }

    it("should report a version mismatch") {
      PluginManifest.read(guid1, ClassLoader.getSystemResource("wrongVersion").getPath()) match {
        case Right(_) => fail("expected error value")
        case Left(error) => error match {
          case ManifestReadError.VersionMismatch() => {}
          case x => fail("expected VersionMismatch, got " + x.getClass().getSimpleName())
        }
      }
    }

    it("should correctly read a well-formed manifest") {
      PluginManifest.read(guid1, ClassLoader.getSystemResource("goodManifest").getPath()) match {
        case Left(x) => fail("did not expect an error value, got " + x.getClass().getSimpleName())
        case Right(list) => assert(correct(list))
      }
    }

    it("should correctly read back the manifest that it has written") {
      PluginManifest.write(guid1, "target/test-classes/testManifest", List("org.workcraft.GoodPluginA", "org.workcraft.GoodPluginB")) match {
        case Some(error) => fail("did not expect an error value while writing, got " + error.getClass().getSimpleName())
        case None => PluginManifest.read(guid1, "target/test-classes/testManifest") match {
          case Left(x) => fail("did not expect an error value while reading, got " + x.getClass().getSimpleName())
          case Right(list) => assert(correct(list))
        }
      }
    }
  }
}
