/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.grpc.client.runtime.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "com.google.protobuf.UnsafeUtil")
final class Target_com_google_protobuf_UnsafeUtil {

    @Substitute
    private static sun.misc.Unsafe getUnsafe() {
        // since we configured Netty with io.netty.noUnsafe=true, this should be safe
        return null;
    }
}

class GrpcNettySubstitutions {
}
