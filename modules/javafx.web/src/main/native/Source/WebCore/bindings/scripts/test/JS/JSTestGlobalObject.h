/*
    This file is part of the WebKit open source project.
    This file has been generated by generate-bindings.pl. DO NOT MODIFY!

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Library General Public
    License as published by the Free Software Foundation; either
    version 2 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Library General Public License for more details.

    You should have received a copy of the GNU Library General Public License
    along with this library; see the file COPYING.LIB.  If not, write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA 02110-1301, USA.
*/

#pragma once

#include "JSDOMWrapper.h"
#include "TestGlobalObject.h"
#include <wtf/NeverDestroyed.h>

namespace WebCore {

class TestGlobalObject;

class JSTestGlobalObject : public JSDOMWrapper<TestGlobalObject> {
public:
    using Base = JSDOMWrapper<TestGlobalObject>;
    static JSTestGlobalObject* create(JSC::Structure* structure, JSDOMGlobalObject* globalObject, Ref<TestGlobalObject>&& impl)
    {
        JSTestGlobalObject* ptr = new (NotNull, JSC::allocateCell<JSTestGlobalObject>(globalObject->vm().heap)) JSTestGlobalObject(structure, *globalObject, WTFMove(impl));
        ptr->finishCreation(globalObject->vm());
        return ptr;
    }

    static const bool needsDestruction = false;

    static TestGlobalObject* toWrapped(JSC::VM&, JSC::JSValue);
    static void destroy(JSC::JSCell*);

    DECLARE_INFO;

    static JSC::Structure* createStructure(JSC::VM& vm, JSC::JSGlobalObject* globalObject, JSC::JSValue prototype)
    {
        return JSC::Structure::create(vm, globalObject, prototype, JSC::TypeInfo(JSC::GlobalObjectType, StructureFlags), info());
    }

    static JSC::JSValue getConstructor(JSC::VM&, const JSC::JSGlobalObject*);
public:
    static const unsigned StructureFlags = JSC::HasStaticPropertyTable | Base::StructureFlags;
protected:
    JSTestGlobalObject(JSC::Structure*, JSDOMGlobalObject&, Ref<TestGlobalObject>&&);

    void finishCreation(JSC::VM&);
};

class JSTestGlobalObjectOwner : public JSC::WeakHandleOwner {
public:
    virtual bool isReachableFromOpaqueRoots(JSC::Handle<JSC::Unknown>, void* context, JSC::SlotVisitor&);
    virtual void finalize(JSC::Handle<JSC::Unknown>, void* context);
};

inline JSC::WeakHandleOwner* wrapperOwner(DOMWrapperWorld&, TestGlobalObject*)
{
    static NeverDestroyed<JSTestGlobalObjectOwner> owner;
    return &owner.get();
}

inline void* wrapperKey(TestGlobalObject* wrappableObject)
{
    return wrappableObject;
}

JSC::JSValue toJS(JSC::ExecState*, JSDOMGlobalObject*, TestGlobalObject&);
inline JSC::JSValue toJS(JSC::ExecState* state, JSDOMGlobalObject* globalObject, TestGlobalObject* impl) { return impl ? toJS(state, globalObject, *impl) : JSC::jsNull(); }
JSC::JSValue toJSNewlyCreated(JSC::ExecState*, JSDOMGlobalObject*, Ref<TestGlobalObject>&&);
inline JSC::JSValue toJSNewlyCreated(JSC::ExecState* state, JSDOMGlobalObject* globalObject, RefPtr<TestGlobalObject>&& impl) { return impl ? toJSNewlyCreated(state, globalObject, impl.releaseNonNull()) : JSC::jsNull(); }

class JSTestGlobalObjectPrototype : public JSC::JSNonFinalObject {
public:
    using Base = JSC::JSNonFinalObject;
    static JSTestGlobalObjectPrototype* create(JSC::VM& vm, JSC::JSGlobalObject* globalObject, JSC::Structure* structure)
    {
        JSTestGlobalObjectPrototype* ptr = new (NotNull, JSC::allocateCell<JSTestGlobalObjectPrototype>(vm.heap)) JSTestGlobalObjectPrototype(vm, globalObject, structure);
        ptr->finishCreation(vm);
        return ptr;
    }

    DECLARE_INFO;
    static JSC::Structure* createStructure(JSC::VM& vm, JSC::JSGlobalObject* globalObject, JSC::JSValue prototype)
    {
        return JSC::Structure::create(vm, globalObject, prototype, JSC::TypeInfo(JSC::ObjectType, StructureFlags), info());
    }

private:
    JSTestGlobalObjectPrototype(JSC::VM& vm, JSC::JSGlobalObject*, JSC::Structure* structure)
        : JSC::JSNonFinalObject(vm, structure)
    {
    }
public:
    static const unsigned StructureFlags = JSC::HasStaticPropertyTable | Base::StructureFlags;
};

template<> struct JSDOMWrapperConverterTraits<TestGlobalObject> {
    using WrapperClass = JSTestGlobalObject;
    using ToWrappedReturnType = TestGlobalObject*;
};

} // namespace WebCore
