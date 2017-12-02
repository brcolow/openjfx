/*
 * Copyright (C) 2010, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1.  Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2.  Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE INC. AND ITS CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL APPLE INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#pragma once

#include "AudioBasicInspectorNode.h"
#include "RealtimeAnalyser.h"

namespace WebCore {

class AnalyserNode final : public AudioBasicInspectorNode {
public:
    static Ref<AnalyserNode> create(AudioContext& context, float sampleRate)
    {
        return adoptRef(*new AnalyserNode(context, sampleRate));
    }

    virtual ~AnalyserNode();

    unsigned fftSize() const { return m_analyser.fftSize(); }
    ExceptionOr<void> setFftSize(unsigned);

    unsigned frequencyBinCount() const { return m_analyser.frequencyBinCount(); }

    ExceptionOr<void> setMinDecibels(double);
    double minDecibels() const { return m_analyser.minDecibels(); }

    ExceptionOr<void> setMaxDecibels(double);
    double maxDecibels() const { return m_analyser.maxDecibels(); }

    ExceptionOr<void> setSmoothingTimeConstant(double);
    double smoothingTimeConstant() const { return m_analyser.smoothingTimeConstant(); }

    void getFloatFrequencyData(const RefPtr<JSC::Float32Array>& array) { m_analyser.getFloatFrequencyData(array.get()); }
    void getByteFrequencyData(const RefPtr<JSC::Uint8Array>& array) { m_analyser.getByteFrequencyData(array.get()); }
    void getByteTimeDomainData(const RefPtr<JSC::Uint8Array>& array) { m_analyser.getByteTimeDomainData(array.get()); }

private:
    AnalyserNode(AudioContext&, float sampleRate);

    void process(size_t framesToProcess) final;
    void reset() final;

    double tailTime() const final { return 0; }
    double latencyTime() const final { return 0; }

    RealtimeAnalyser m_analyser;
};

} // namespace WebCore
