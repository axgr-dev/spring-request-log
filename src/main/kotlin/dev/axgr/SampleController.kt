package dev.axgr

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {

  @PostMapping("/sample")
  fun sample(@RequestBody request: SampleRequest) = SampleResponse("Hello, ${request.name}!")
}

data class SampleRequest(val name: String)
data class SampleResponse(val message: String)
