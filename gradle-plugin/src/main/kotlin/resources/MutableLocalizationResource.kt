package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborArray
import net.derfruhling.serenity.gradle.resources.dynamic.PhonyDynamicString
import net.derfruhling.serenity.gradle.resources.dynamic.PhonyDynamicStringComponent
import net.derfruhling.serenity.gradle.resources.dynamic.PhonyDynamicStringComponent.*
import net.openhft.hashing.LongHashFunction
import org.gradle.api.InvalidUserDataException
import org.xml.sax.Attributes
import org.xml.sax.Locator
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.*
import javax.xml.parsers.SAXParserFactory

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@CborArray
class MutableLocalizationResource(
    val tag: String,
    val strings: MutableMap<Long, PhonyDynamicString> = mutableMapOf()
) {
    private sealed class Context {
        var namePrefix: List<String>? = null
    }

    private data object Root : Context()
    private data class Group(val name: String) : Context()
    private data class Rule(val name: String) : Context()

    private open class RuleBuilder {
        companion object {
            private val EMPTY = StringConst("")
        }

        open var dynamicString: PhonyDynamicString? = null

        fun appendText(text: String) {
            dynamicString = if(dynamicString == null) {
                PhonyDynamicString(StringConst(text))
            } else {
                PhonyDynamicString(when(val orig = dynamicString!!.component) {
                    is StringConst -> {
                        StringConst(orig.text.trim() + ' ' + text)
                    }

                    is Sequence -> Sequence(orig.children + StringConst(text))

                    else -> Sequence(listOf(
                        orig,
                        StringConst(text)
                    ))
                })
            }
        }

        fun append(component: PhonyDynamicStringComponent) {
            dynamicString = if(dynamicString == null) {
                PhonyDynamicString(component)
            } else {
                PhonyDynamicString(when(val orig = dynamicString!!.component) {
                    is Sequence -> Sequence(orig.children + component)
                    else -> Sequence(listOf(dynamicString!!.component, component))
                })
            }
        }

        fun toBold(): Bold {
            return Bold(dynamicString?.component ?: EMPTY)
        }

        fun toItalic(): Italic {
            return Italic(dynamicString?.component ?: EMPTY)
        }

        fun build(): PhonyDynamicString = dynamicString ?: PhonyDynamicString(EMPTY)
    }

    private class ArgRuleBuilder(val argument: Argument) : RuleBuilder() {
        override var dynamicString: PhonyDynamicString?
            get() = super.dynamicString
            set(value) {
                argument.fallback = value?.component
                super.dynamicString = value
            }
    }

    private inner class LocalizationResourceXmlParser : DefaultHandler() {
        private val stack = Stack<Context>()
        private val hasher = LongHashFunction.xx3()!!

        private val namePrefix: List<String>
            get() {
                val last = stack.peek() ?: return emptyList()
                last.namePrefix?.let { return it }

                return stack.mapNotNull {
                    if(it is Group) it.name
                    else null
                }.also { last.namePrefix = it }
            }

        private val isRule: Boolean
            get() = stack.any { it is Rule }

        private var mutableRuleStack = Stack<RuleBuilder>()
        private var whitespace = " "
        private lateinit var locator: Locator

        override fun setDocumentLocator(locator: Locator) {
            this.locator = locator
        }

        override fun startElement(
            uri: String,
            localName: String,
            qName: String,
            attributes: Attributes
        ) {
            if(stack.empty()) {
                attributes.getValue("whitespace")?.let { whitespace = it }
                stack.push(Root)
            } else if (!isRule) {
                when(attributes.getValue("type")) {
                    "group" -> stack.push(Group(qName))
                    else -> {
                        stack.push(Rule((namePrefix + qName).joinToString("/")))
                        mutableRuleStack.push(RuleBuilder())
                    }
                }
            } else {
                when(qName) {
                    "b", "i" -> {
                        mutableRuleStack.push(RuleBuilder())
                    }

                    "arg" -> {
                        val index = attributes.getValue("of").toInt()
                        val argument = Argument(index)
                        mutableRuleStack.lastElement().append(argument)
                        mutableRuleStack.push(ArgRuleBuilder(argument))
                    }

                    else -> {
                        throw InvalidUserDataException("At ${locator.lineNumber}:${locator.columnNumber}: unknown element: $qName")
                    }
                }
            }
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            if(isRule) {
                val last = mutableRuleStack.pop()!!

                if(mutableRuleStack.empty()) {
                    strings[hasher.hashBytes((stack.pop() as Rule).name.toByteArray())] =
                        last.build()
                } else when(qName) {
                    "b" -> mutableRuleStack.peek()!!.append(last.toBold())
                    "i" -> mutableRuleStack.peek()!!.append(last.toItalic())
                }
            } else {
                stack.pop()
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            if(isRule) {
                var text = ch.concatToString(start, start + length).replace(WHITESPACE_REGEX, whitespace)
                if(mutableRuleStack.size == 1 && mutableRuleStack.peek().dynamicString == null) {
                    text = text.trimStart()
                }
                mutableRuleStack.peek()!!.appendText(text)
            }
        }
    }

    fun parseFromXml(file: File) {
        val saxParser = SAXParserFactory.newDefaultInstance().newSAXParser()!!
        val handler = LocalizationResourceXmlParser()
        saxParser.parse(file, handler)
    }

    companion object {
        private val WHITESPACE_REGEX = Regex("""[ \n\r\t]+""")
    }
}