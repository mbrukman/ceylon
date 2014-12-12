package com.redhat.ceylon.compiler.js;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.model.Parameter;
import com.redhat.ceylon.compiler.typechecker.model.ProducedType;
import com.redhat.ceylon.compiler.typechecker.model.TypeDeclaration;
import com.redhat.ceylon.compiler.typechecker.model.TypeParameter;
import com.redhat.ceylon.compiler.typechecker.model.Util;
import com.redhat.ceylon.compiler.typechecker.model.Value;
import com.redhat.ceylon.compiler.typechecker.tree.Node;

public class SerializationHelper {

    /** Add serialize method to a class. Can be on the prototype or the instance, depending on the style being used. */
    static void addSerializer(final Node node, final com.redhat.ceylon.compiler.typechecker.model.Class d,
            final GenerateJsVisitor gen) {
        final String dc = gen.getNames().createTempVariable();
        final String ac = gen.getNames().createTempVariable();
        gen.out(gen.getNames().self(d), ".ser$$=function(", dc, ")");
        gen.beginBlock();
        gen.out("var ", gen.getNames().self(d), "=this;");
        //Call super.ser$$ if possible
        com.redhat.ceylon.compiler.typechecker.model.Class et = d.getExtendedTypeDeclaration();
        while (et != null && !(et.equals(d.getUnit().getObjectDeclaration()) || et.equals(d.getUnit().getBasicDeclaration()))) {
            if (et.isSerializable()) {
                gen.qualify(node, et);
                gen.out(gen.getNames().name(et), ".$$.prototype.ser$$.call(this,", dc, ");");
                et = null;
                gen.endLine();
            } else {
                et = et.getExtendedTypeDeclaration();
            }
        }
        //Get the deconstructor for this class
        gen.out("var ", ac, "=", dc, "(");
        final String classOrMemberClass = d.isMember() ? "MemberClass" : "Class";
        if (JsCompiler.isCompilingLanguageModule()) {
            gen.out("Applied", classOrMemberClass, "$jsint");
        } else {
            gen.out(gen.getClAlias(), "$init$Applied", classOrMemberClass, "$jsint()");
        }
        gen.out("(", gen.getNames().name(d), ",{Type$Applied", classOrMemberClass, ":{t:",
                gen.getNames().name(d));
        if (!d.getTypeParameters().isEmpty()) {
            gen.out(",a:{");
            boolean first=true;
            for (TypeParameter tp : d.getTypeParameters()) {
                if (first)first=false;else gen.out(",");
                gen.out(tp.getName(), "$", d.getName(), ":this.$$targs$$.", tp.getName(), "$", d.getName());
            }
            gen.out("}");
        }
        gen.out("},Arguments$Applied", classOrMemberClass, ":{t:");
        if (d.getParameterList().getParameters().isEmpty()) {
            gen.out(gen.getClAlias(), "Empty");
        } else {
            gen.out("'T',l:[");
            boolean first=true;
            for (Parameter param : d.getParameterList().getParameters()) {
                if (first)first=false;else gen.out(",");
                TypeUtils.typeNameOrList(node, param.getType(), gen, false);
            }
            gen.out("]");
        }
        gen.out("}");
        if (d.isMember()) {
            TypeDeclaration parentd = Util.getContainingClassOrInterface(d);
            gen.out(",Container$MemberClass:{t:", gen.getNames().name(parentd));
            if (!parentd.getTypeParameters().isEmpty()) {
                gen.out(",a:this.outer$.$$targs$$");
            }
            gen.out("}");
        }
        gen.out("}));");
        gen.endLine();
        //Put the outer instance if it's a member
        if (d.isMember()) {
            gen.out(ac, ".putOuterInstance(this.outer$,{Instance$putOuterInstance:",
                    gen.getNames().name(Util.getContainingDeclaration(d)), "});");
            gen.endLine();
        }
        //Now the type arguments
        for (TypeParameter tp : d.getTypeParameters()) {
            gen.out(ac,  ".putTypeArgument(", gen.getClAlias(), "OpenTypeParam$jsint(",
                    gen.getNames().name(d), ",'", tp.getName(), "$", d.getName(), "'),", gen.getClAlias(),
                    "typeLiteral$meta({Type$typeLiteral:");
            gen.out("this.$$targs$$.", tp.getName(), "$", d.getName(), "}));");
            gen.endLine();
        }
        //Get the type's package
        String pkgname = d.getUnit().getPackage().getNameAsString();
        if ("ceylon.language".equals(pkgname)) {
            pkgname = "$";
        }
        //Serialize each value
        List<Value> vals = serializableValues(d);
        if (vals.size() > 1) {
            final String pkvar = gen.getNames().createTempVariable();
            gen.out("var ", pkvar, "=", gen.getClAlias(), "lmp$(ex$,'", pkgname, "');");
            gen.endLine();
            pkgname = pkvar;
        } else {
            pkgname = gen.getClAlias() + "lmp$(ex$,'" + pkgname + "')";
        }
        for (Value v : vals) {
            final TypeDeclaration vd = v.getType().getDeclaration();
            gen.out(ac, ".putValue(", gen.getClAlias(), "OpenValue$jsint(",
                    pkgname, ",this.$prop$", gen.getNames().getter(v),")", ",",
                    "this.", gen.getNames().name(v), ",{Instance$putValue:");
            if (vd instanceof TypeParameter && vd.getContainer() == d) {
                gen.out("this.$$targs$$.", vd.getName(), "$", d.getName());
            } else {
                TypeUtils.typeNameOrList(node, v.getType(), gen, false);
            }
            gen.out("});");
            gen.endLine();
        }
        gen.endBlockNewLine();
    }
    /** Add deserialize method to a class. This one resides directly under the class constructor, since it creates
     * an uninitialized instance and adds state to it. */
    static void addDeserializer(final Node that, final com.redhat.ceylon.compiler.typechecker.model.Class d,
            final GenerateJsVisitor gen) {
        final String dc = gen.getNames().createTempVariable();
        final String cmodel = gen.getNames().createTempVariable();
        final String ni = gen.getNames().self(d);
        final String typename = gen.getNames().name(d);
        gen.out(typename, ".deser$$=function(", dc, ",", cmodel, ",", ni, ")");
        gen.beginBlock();
        if (d.isMember()) {
            gen.out("//TODO getOuterInstance");
            gen.endLine();
        }
        if (!d.isAbstract()) {
            gen.out("if(", ni, "===undefined)");
            gen.beginBlock();
            gen.out(ni, "=new ", gen.getNames().name(d), ".$$;");
            gen.endLine();
            //Set type arguments, if any
            boolean first=true;
            for (TypeParameter tp : d.getTypeParameters()) {
                if (first) {
                    gen.out(gen.getClAlias(), "set_type_args(", ni, ",{");
                    first=false;
                } else {
                    gen.out(",");
                }
                gen.out(tp.getName(), "$", d.getName(), ":", cmodel, ".$$targs$$.Type$Class.a.",
                        tp.getName(), "$", d.getName());
            }
            if (!first) {
                gen.out("});");
                gen.endLine();
            }
            setDeserializedTypeArguments(d, d.getExtendedType(), true, that, ni, gen, new HashSet<TypeDeclaration>());
            gen.endBlockNewLine();
        }
        //Call super.deser$$ if possible
        boolean create = true;
        com.redhat.ceylon.compiler.typechecker.model.Class et = d.getExtendedTypeDeclaration();
        while (create && !(et.equals(that.getUnit().getObjectDeclaration()) || et.equals(that.getUnit().getBasicDeclaration()))) {
            if (et.isSerializable()) {
                gen.qualify(that, et);
                gen.out(gen.getNames().name(et), ".deser$$(", dc, ",", cmodel, ",", ni, ");");
                gen.endLine();
                create = false;
            } else {
                et = et.getExtendedTypeDeclaration();
            }
        }
        //Get the current package and module
        String pkgname = d.getUnit().getPackage().getNameAsString();
        if ("ceylon.language".equals(pkgname)) {
            pkgname = "$";
        }
        List<Value> vals = serializableValues(d);
        if (vals.size() > 1) {
            final String pkvar = gen.getNames().createTempVariable();
            gen.out("var ", pkvar, "=", gen.getClAlias(), "lmp$(ex$,'", pkgname, "');");
            gen.endLine();
            pkgname = pkvar;
        } else {
            pkgname = gen.getClAlias() + "lmp$(ex$,'" + pkgname + "')";
        }
        //Deserialize each value
        for (Value v : vals) {
            final TypeDeclaration vd = v.getType().getDeclaration();
            final String valname = v.isParameter() || v.isLate() ?
                    gen.getNames().name(v)+"_" : gen.getNames().privateName(v);
            gen.out(ni, ".", valname, "=", dc, ".getValue(", gen.getClAlias(),
                    "OpenValue$jsint(", pkgname, ",", gen.getNames().name(d),".$$.prototype.$prop$",
                    gen.getNames().getter(v),")", ",{Instance$getValue:");
            if (vd instanceof TypeParameter && vd.getContainer() == d) {
                gen.out(ni, ".$$targs$$.", vd.getName(), "$", d.getName());
            } else {
                TypeUtils.typeNameOrList(that, v.getType(), gen, false);
            }
            gen.out("});");
            gen.endLine();
        }
        if (!d.isAbstract()) {
            gen.out("return ", ni, ";");
        }
        gen.endBlockNewLine();
    }

    /** Recursively add all the type arguments from extended and satisfied types. */
    private static void setDeserializedTypeArguments(final com.redhat.ceylon.compiler.typechecker.model.Class root,
            ProducedType pt, boolean first, final Node that, final String ni, final GenerateJsVisitor gen,
            final Set<TypeDeclaration> decs) {
        if (pt == null) {
            return;
        }
        final boolean start=decs.isEmpty();
        final List<ProducedType> sats = start ? root.getSatisfiedTypes() : pt.getSatisfiedTypes();
        decs.add(root);
        while (pt != null && !root.getUnit().getBasicDeclaration().equals(pt.getDeclaration())) {
            if (!decs.contains(pt.getDeclaration())) {
                for (Map.Entry<TypeParameter,ProducedType> tp : pt.getTypeArguments().entrySet()) {
                    if (first) {
                        gen.out(gen.getClAlias(), "set_type_args(", ni, ",{");
                        first=false;
                    } else {
                        gen.out(",");
                    }
                    gen.out(tp.getKey().getName(), "$", pt.getDeclaration().getName(), ":");
                    TypeUtils.typeNameOrList(that, tp.getValue(), gen, false);
                }
                decs.add(pt.getDeclaration());
                setDeserializedTypeArguments(root, pt, first, that, ni, gen, decs);
            }
            pt = pt.getSupertype(root);
        }
        for (ProducedType sat : sats) {
            if (!decs.contains(sat.getDeclaration())) {
                for (Map.Entry<TypeParameter,ProducedType> tp : sat.getTypeArguments().entrySet()) {
                    if (first) {
                        gen.out(gen.getClAlias(), "set_type_args(", ni, ",{");
                        first=false;
                    } else {
                        gen.out(",");
                    }
                    gen.out(tp.getKey().getName(), "$", sat.getDeclaration().getName(), ":");
                    TypeUtils.typeNameOrList(that, tp.getValue(), gen, false);
                }
                decs.add(sat.getDeclaration());
            }
            setDeserializedTypeArguments(root, sat, first, that, ni, gen, decs);
        }
        if (!first && start) {
            gen.out("});");
            gen.endLine();
        }
    }

    static List<Value> serializableValues(com.redhat.ceylon.compiler.typechecker.model.Class d) {
        ArrayList<Value> vals = new ArrayList<>();
        for (Declaration m : d.getMembers()) {
            if (!m.isFormal() && m instanceof Value && !m.isSetter()) {
                Value v = (Value)m;
                if (!v.isTransient()) {
                    vals.add(v);
                }
            }
        }
        return vals;
    }

}
